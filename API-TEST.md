# API 테스트 시나리오

## 기본 설정

```bash
# API 기본 URL
BASE_URL="http://localhost:8080"

# 응답을 저장할 변수들
ACCESS_TOKEN=""
REFRESH_TOKEN=""
ACCOUNT_NUMBER=""
USER_CODE=""
```

## 시나리오 1: 회원가입 및 로그인

```bash
# 1. 휴대폰 인증번호 발송
curl -X POST "${BASE_URL}/register/sms-certification/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userPhone": "01056150777"
  }'

# 2. 휴대폰 인증번호 확인
curl -X POST "${BASE_URL}/register/sms-certification/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "userPhone": "01056150777",
    "verificationCode": "949049"
  }'

# 3. 회원가입
curl -X POST "${BASE_URL}/register" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser",
    "userPwd": "Test123!",
    "userNameKr": "홍길동",
    "userNameEn": "Hong Gil Dong",
    "userEmail": "test@example.com",
    "userInherentNumber": "9001011234567",
    "userPhone": "01056150777",
    "userAddr": "서울시 강남구",
    "userAddrDetail": "테헤란로 123"
  }'

# 4. 로그인
curl -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser",
    "userPwd": "Test123!"
  }' | jq -r '.data.accessToken' > access_token.txt

ACCESS_TOKEN=$(cat access_token.txt)
```

## 시나리오 2: 계좌 생성 및 이체

```bash
# 1. 계좌 생성 (번호 자동생성되니까 아무숫자나 넣어도 됨)
curl -X POST "${BASE_URL}/accounts/open" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "1234567890",
    "accountPassword": "1234",
    "pinNumber": "123456",
    "accountType": "SAVINGS",
    "balance": 1000000
  }'

# 1.1 계좌 생성
curl -X POST "${BASE_URL}/accounts/open" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "987654321",
    "accountPassword": "1234",
    "pinNumber": "123456",
    "accountType": "SAVINGS",
    "balance": 3000000
  }'

# 2. 계좌 목록 조회 (여기서 생성된 계좌번호 잘 봐야됨)
curl -X GET "${BASE_URL}/accounts" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"

# 3. 즉시 이체 (생성된 계좌번호 입력)
curl -X POST "${BASE_URL}/transfer/immediately" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "senderAccountNumber": "04-27705-0135573",
    "receiverAccountNumber": "04-31319-3636992",
    "amount": 100000,
    "accountPassword": "1234"
  }'

# 4. 잔액 조회 (생성된 계좌번호 입력)
curl -X GET "${BASE_URL}/accounts/balance?account_number=04-27705-0135573" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"

# 5. 거래내역 조회 
curl -X GET "${BASE_URL}/accounts/transactions?account_number=04-27705-0135573" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

## 시나리오 3: 자동이체 설정

```bash
# 1. 자동이체 설정
curl -X POST "${BASE_URL}/transfer/auto" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "senderAccountNumber": "04-27705-0135573",
    "receiverAccountNumber": "04-31319-3636992",
    "amount": 100000,
    "scheduledDate": "2025-01-25",
    "accountPassword": "1234",
    "transferDay": 25
  }'

# 2. 자동이체 목록 조회 (USER_CODE는 시나리오6-1에서 볼 수 있음)
curl -X GET "${BASE_URL}/transfer/autoList/${USER_CODE}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

## 시나리오 4: 관리자 기능

```bash
# 1. 관리자 로그인 (눈치껏 보면됨)
curl -X POST "${BASE_URL}/admin/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "adminId": "admin",
    "password": "password"
  }' | jq -r '.data' > admin_token.txt

ADMIN_TOKEN=$(cat admin_token.txt)

# 2. 공지사항 생성
curl -X POST "${BASE_URL}/admin/notices" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "시스템 점검 안내",
    "content": "2025년 1월 17일 새벽 2시부터 4시까지 시스템 점검이 있을 예정입니다.",
    "important": true
  }'

# 3. 공지사항 목록 조회
curl -X GET "${BASE_URL}/admin/notices" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"

# 4. 특정 공지사항 조회
curl -X GET "${BASE_URL}/admin/notices/1" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"

# 5. 사용자 목록 조회
curl -X GET "${BASE_URL}/admin/users" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"

# 6. 계좌 목록 조회
curl -X GET "${BASE_URL}/admin/accounts" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

## 시나리오 5: 사용자 정보 관리 (여기는 안되는것 같은데)

```bash
# 1. 사용자 정보 조회
curl -X GET "${BASE_URL}/users/user-info" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"

# 2. 사용자 정보 수정
curl -X PUT "${BASE_URL}/users/user-info" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "userPwd": "NewTest123!@#",
    "userEmail": "newemail@example.com",
    "userPhone": "01087654321",
    "userAddr": "서울시 서초구",
    "userAddrDetail": "서초대로 123",
    "userTrsfLimit": 5000000,
    "userMainAcc": "1234567890"
  }'

# 3. 직업 정보 등록/수정
curl -X PUT "${BASE_URL}/users/job-info" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "jobName": "개발자",
    "companyName": "테크컴퍼니",
    "companyAddr": "서울시 강남구 테헤란로 123",
    "companyPhone": "02-1234-5678"
  }'
```

## 시나리오 6: 파일 업로드/다운로드 (자신 없으면 하지 말기)

```bash
# 1. 파일 업로드를 위한 사용자 코드 조회
curl -X GET "${BASE_URL}/users/uploader" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"

# 2. 단일 파일 업로드 이건 base64 인코딩 해야함 테스트 하지 마세요
curl -X POST "${BASE_URL}/files/upload" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/Users/sehun/newnewnew/piggybank-spring/test.png" \
  -F "request={\"userCode\":\"${USER_CODE}\"}"

# 3. 파일 다운로드
curl -X GET "${BASE_URL}/files/download?filePath=/uploaded/test.png" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  --output downloaded_file.png
```

## 시나리오 7: 로그아웃 

```bash
# 1. 로그아웃
curl -X POST "${BASE_URL}/signout" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "accessToken": "'${ACCESS_TOKEN}'",
    "refreshToken": "'${REFRESH_TOKEN}'"
  }'

# 2. 관리자 로그아웃
curl -X POST "${BASE_URL}/admin/auth/logout" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```
