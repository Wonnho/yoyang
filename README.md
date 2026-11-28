# 국내 투자정보 REST API 시작 가이드

국내 개인투자자가 코스피·코스닥 지수, 원·달러 환율, 금 선물, 한국은행 기준금리와 상장기업 기본정보를 조회하는 API입니다. 첫 배포는 공개 조회를 기준으로 설계했습니다. 첫해 목표인 가입 회원 500명은 회원 기능 도입 후 중복을 제거한 가입 완료 계정 수로 측정합니다.

## API 계약

경로와 전체 응답 스키마는 [openapi.yaml](openapi.yaml)에 있습니다. 기준 경로는 `/api/v1`입니다.

| 메서드와 경로 | 반환 내용 | 상태 |
| --- | --- | --- |
| `GET /status` | 앱 응답 여부 | 구현됨 |
| `GET /market-summary` | 시장지표 다섯 항목의 최신 공표값 | 설계 |
| `GET /companies?query=005930&page=1&size=20` | 기업명 또는 종목코드로 찾은 상장기업 목록 | 설계 |
| `GET /companies/{corpCode}` | OpenDART 고유번호로 찾은 기업 기본정보 | 설계 |

시세 화면은 `observationDate`로 각 지표의 기준일을 표시합니다. 오늘 공표분이 예정되기 전과 휴장일에는 직전 공표값을 `current`로 반환할 수 있습니다. 공표 예정 시점과 공급자 지연 허용 시간을 지난 뒤에도 업데이트가 없으면 `stale`을 반환합니다. 기준금리는 현재 유효한 정책금리를 표시하고, 해당 기관의 발표와 적용 일정을 기준으로 상태를 판단합니다.

각 지표는 저장값이 없으면 `unavailable`과 `observation: null`을 반환합니다. 하나라도 저장값이 있으면 시장 요약은 `200`을 반환하고, 전체 시세를 읽을 수 없으면 `503`을 반환합니다. 기업 검색 결과가 비어 있으면 `200`과 빈 배열을 반환합니다. 기업 상세의 8자리 형식이 틀리면 `400`, 조회한 기업이 없으면 `404`입니다. 오류 본문은 `application/problem+json`과 고정된 `code`를 사용합니다.

다음은 시장 요약의 `usdKrw` 항목 예시입니다. 금액과 날짜는 샘플 값입니다.

```json
{
  "dataStatus": "current",
  "observation": {
    "value": "1350.25",
    "unit": "KRW_PER_USD",
    "observationDate": "2026-11-26",
    "observationType": "REFERENCE_RATE",
    "source": "한국은행 ECOS"
  }
}
```

## PostEngine에서 참고한 구조

PostEngine의 `ArticleApiController`는 복수형 리소스 경로에서 목록과 상세를 나누고 `ArticleService`로 조회를 위임합니다. 서비스는 `ArticleRepository`로 저장 데이터에 접근합니다. 이 구조를 `CompanyController` → `CompanyService` → `CompanyRepository`에 적용합니다. 현재 프로젝트의 `/api/v1` 접두사를 따르고 응답 전용 DTO의 필드명은 `corpCode`, `observationDate`처럼 `lowerCamelCase`로 작성합니다. [PostEngine 컨트롤러](https://github.com/Wonnho/postengine/blob/0a2ca6406024f4774c0c8796f8b23e2f59e200dd/src/main/java/com/postengine/post/api/ArticleApiController.java), [PostEngine 서비스](https://github.com/Wonnho/postengine/blob/0a2ca6406024f4774c0c8796f8b23e2f59e200dd/src/main/java/com/postengine/post/service/ArticleService.java)

## 로컬에서 시작하기

JDK 17 이상과 Maven이 필요합니다. 프로젝트 루트에서 다음을 실행합니다. 첫 실행에는 Maven 의존성을 내려받을 수 있어야 합니다. IntelliJ IDEA의 Maven 실행 구성에서도 아래 `spring-boot:run` 목표와 `-D` 옵션을 사용할 수 있습니다.

```bash
java -version
mvn -version
mvn spring-boot:run \
  -Dspring-boot.run.arguments='--spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration --server.address=127.0.0.1 --server.port=8087'
```

다른 터미널에서 기존 상태 API를 호출합니다. 기대 응답은 `{"status":"ok"}`입니다. 이 실행 모드는 MySQL 없이 상태 API를 확인할 때 사용합니다.

```bash
curl --fail-with-body http://127.0.0.1:8087/api/v1/status
```

기업과 시세 저장소를 구현할 때는 로컬 개발용 MySQL 데이터베이스와 전용 계정을 준비합니다. 현재 `application.properties`가 읽는 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 셸 또는 IDE 실행 환경에 설정합니다. URL 형식은 `jdbc:mysql://127.0.0.1:3306/investment`입니다. 환경변수를 설정한 뒤 `mvn spring-boot:run`을 실행하면 기본 포트 `8087`을 사용합니다. 필요하면 `APP_PORT`로 바꿀 수 있습니다.

## 첫 구현 순서

1. **API 응답부터 연결합니다.** `MarketSummaryController`와 `CompanyController`를 추가하고 샘플 데이터로 OpenAPI 계약을 충족시킵니다. 시세 일부 누락 시 `200`, 전체 누락 시 `503`, 기업 검색 결과 없음은 빈 배열, 미등록 기업 상세는 `404`를 확인합니다.
2. **기업과 시세를 저장합니다.** 기업은 `corpCode`를 키로 두고 `stockCode`와 기업명으로 검색합니다. 시세는 지표와 공급자 기준일을 키로 두고, 금 선물에는 결제월도 포함합니다. 수집 작업은 같은 키를 갱신할 수 있게 구성해 재시도 때 데이터가 중복되지 않도록 합니다.
3. **원천 데이터를 하나씩 연결합니다.** 원천 데이터는 스케줄러가 수집하고 API는 저장된 최신 공표값을 읽습니다. 공급자별 공표 일정과 시간대를 설정하고 공개 배포 전에 데이터 사용 조건을 확인합니다.

| 지표 또는 정보 | 원천과 표시 기준 |
| --- | --- |
| 코스피·코스닥 | KRX 일별 종가, `INDEX_POINTS`, `CLOSE`. [KRX OPEN API 서비스 목록](https://openapi.krx.co.kr/contents/OPP/INFO/service/OPPINFO004.cmd) |
| 원·달러 환율 | ECOS 일별 원·달러 기준환율, `KRW_PER_USD`, `REFERENCE_RATE`. [한국은행 ECOS API](https://ecos.bok.or.kr/api/) |
| 금 선물 | CME COMEX GC 일별 정산가, `USD_PER_TROY_OUNCE`, `SETTLEMENT`. 기준 거래일에 최종 거래일까지 남아 있고 정산값이 있는 계약 중 가장 가까운 결제월을 표시합니다. [CME 금 선물 명세](https://www.cmegroup.com/markets/metals/precious/gold/specs) |
| 한국은행 기준금리 | ECOS 기준금리, `PERCENT`, `POLICY_RATE`. `3.00`은 3%입니다. [한국은행 ECOS API](https://ecos.bok.or.kr/api/) |
| 기업 기본정보 | OpenDART 고유번호 목록으로 종목코드를 매핑하고 기업개황에서 상세 필드를 수집합니다. [OpenDART 공시정보 API](https://opendart.fss.or.kr/guide/main.do?apiGrpCd=DS001), [기업개황 명세](https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS001&apiId=2019002) |
