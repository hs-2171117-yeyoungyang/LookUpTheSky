# LookUpTheSky
현재 위치의 날씨 정보를 알려주는 어플리케이션

## 목표
- 현재 사용자의 위치를 가져올 수 있다.
- 위치에 따른 날씨 정보를 제공할 수 있다.
- 기온에 따라 무엇을 입을지 추천해줄 수 있다.
- 날씨에 대한 이슈가 있다면 공지해줄 수 있다.(ex. 비가 올 확률이 높을 때)
- 날씨는 그날 하루와 관련이 높으므로 각 날짜마다 메모를 남길 수 있다.
- 복잡하지 않은 UI와 적절한 이미지를 제공하여 사용자가 정보를 쉽게 알아볼 수 있다.

## 구현
<img width="455" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/5421ec84-25b9-4b2f-9c08-17b621f3b0ce">

- 기상청의 단기예보 API를 사용한다.
  > 3시간 간격, 하루 8번의 데이터를 가져올 수 있다.
  > 강수확률, 하늘상태, 습도, 풍속, 기온 정보를 이용한다.
  > 격자 x, y값에 따라 날씨 정보를 받아온다.
- GPS를 이용하여 사용자의 위치를 가져온다.
  > 위치 권한이 허용된 경우만 사용할 수 있다.
  > 위도, 경도를 가져온다.
- GeoCoder를 이용한다.
  > 위도와 경도를 입력한다.
  > 주소를 출력한다.
- loaction.xls 파일
  > 특정 주소(ex.00구)를 입력한다.
  > 격자 x, y를 출력한다.

## 화면 설명
<img width="602" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/b6d957e4-d34f-477e-9828-b42c43f54c8e">


- 시작 화면
  > 로고와 어플리케이션의 설명
  > - ![image](https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/a88fffa1-f831-4ba1-b64d-ea4953e0e911)

- 메인 화면
  - 상단
    > 현재 위치의 특정주소(ex.00시 00구) 출력
    > - <img width="182" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/37bafa14-1727-4e0b-9c48-0cbc3dfb3342">
    

  - 배경
    > 시간에 따라 라이트/다크 모드 설정
    > 배경색: 낮-연한 연두색, 밤-회색
    > 
  - 날씨 정보
    > - 기온과 기온 상태(ex.흐림) 표시
    > <img width="172" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/d8aa6193-3d7f-46fd-a120-0922c1e86464">
    
    > - 기온 클릭 시 -> 기온에 따른 텍스트 색상 변경
    > <img width="69" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/1e1a0894-cd26-416e-a5de-3e76121f7a2d">
 
    > - 하늘상태와 하늘상태 이미지
    > <img width="83" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/7ab80573-29f0-44e1-86b5-33731eb93394">
    
    > - 비 올 확률, 습도, 풍속
    > - 풍속의 범위에 따라 강, 중, 약으로 나누고 아이콘 이미지 변경
    > <img width="113" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/d523ea3e-9170-46cb-a9d4-0e7f1f94f9bd">
    
  - 날씨 이슈 공지
    > - 사용자에게 알릴 날씨에 대한 이슈를 알린다.
    > - 이슈에 따라 이미지 변경
    > <img width="62" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/65ce675c-294d-4695-9777-8aca4eb2238f">
    
  - 기온별 옷차림
    > - 기온의 범위(9가지)에 따라 나누고 이미지 변경
    > <img width="176" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/968bb7f8-299c-452f-9e4c-00a6500c9444">
    
  - 메모
    > - 메모 입력 후 날짜 선택, 저장
    > - 메모 수정
    > - 메모 삭제
    > - 날짜 선택 후 메모 보기
    > - <img width="176" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/51738334-91d4-458a-a333-0e2411dc161f">
     
    > - <img width="173" alt="image" src="https://github.com/hs-2171117-yeyoungyang/LookUpTheSky/assets/115926596/070a3d1e-cc0d-4809-acc9-15c62e85bab7">
    
  

## 기대효과
- 사용자에 날씨 정보를 간결하고 시각적으로 보여주어 누구나 쉽게 확인할 수 있다.
- 사용자는 기온에 따라 무엇을 입을지 고민하지 않아도 된다.
- 기기의 상태(시간, 위치)에 따라 변화하는 UI로 흥미를 돋울 수 있다.

## 발표 영상
https://youtu.be/UF26RQxlGQs?si=nG6IUysPxlWPt1UX
