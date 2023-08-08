<div align=center>
    <img src="https://capsule-render.vercel.app/api?type=waving&color=642BF6&height=250&section=header&text=MINI%20PROJECT%2012%20BACK-END&fontSize=50&fontColor=ffffff" />
    <div align=center>
        <h1>📚 기술 스택</h1>
        <h2>🛠️ 개발 환경</h2>
        <img src="https://img.shields.io/badge/java 11-007396?style=for-the-badge&logo=java&logoColor=white">
        <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
        <img src="https://img.shields.io/badge/spring Security-6DB33F?style=for-the-badge&logo=spring Security&logoColor=white">
        <br>
        <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
        <img src="https://img.shields.io/badge/JPA-58FAD0?style=for-the-badge&logo=JPA&logoColor=white">
        <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
        <h2>📢 배포 및 툴</h2>
        <img src="https://img.shields.io/badge/aws-F19E38?style=for-the-badge&logo=amazonaws&logoColor=black">
        <img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=for-the-badge&logo=IntelliJ IDEA&logoColor=white">
        <h2>💬 협업</h2>
        <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
        <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=Slack&logoColor=white">
        <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">
        <img src="https://img.shields.io/badge/Zoom-2D8CFF?style=for-the-badge&logo=Zoom&logoColor=white">
        <h2>➬ <a href="https://github.com/MINI-FASTCAMPUS5/scheduler-front">프론트엔드 깃허브 레포지토리 바로가기</a></h2>
        <h2>👨‍👩‍👧‍👦 팀원 역할</h2>
        <table>
            <tbody>
                <tr>
                    <td align="center" width="200"><a href="https://github.com/a07224">
                        <img src="https://avatars.githubusercontent.com/u/69192549?v=4" width="100px;" alt=""/>
                        <br />
                        <sub><b>강주희 (팀원)</b></sub></a><br />
                    </td>
                    <td align="center" width="200"><a href="https://github.com/k1m2njun">
                        <img src="https://avatars.githubusercontent.com/u/68175311?v=4" width="100px;" alt=""/>
                        <br />
                        <sub><b>길민준 (팀장)</b></sub></a><br />
                    </td>
                    <td align="center" width="200"><a href="https://github.com/backdoor95">
                        <img src="https://avatars.githubusercontent.com/u/109710879?v=4" width="100px;" alt=""/>
                        <br />
                        <sub><b>문준호 (팀원)</b></sub></a><br />
                    </td>
                </tr>
                <tr>
                    <td width="180"><font size=1>
                        - 유저&기획사 메인 페이지,<br />- 유저 티켓팅 신청&취소,<br />- 기획사 검색,<br />- 행사 등록&취소&수정<br />
                    </font></td>
                    <td width="180"><font size=1>
                        -회원가입, JWT 로그인<br />-승인결재 페이지 및 기능 API<br />-AES256 인/디코딩<br />
                        -엑셀파일 다운로드 API<br />-로그인 로그 기록<br />-Eleastic Beanstalk 배포<br />
                        -500에러 파일 출력(log back)<br />-팀 일정 계획, 깃헙 Repo 관리<br />-API명세서 정리<br />-코드 스타일 정리
                    </font></td>
                    <td width="180"><font size=1>
                        - 유저&기획사 마이페이지 <br />- 이미지 업로드용 AWS S3 서버 연결 <br />- 이미지 업로드&삭제 기능 <br />- 회원정보 수정<br />
                    </font></td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

---
## API 명세서
#### 공통
![api명세public](https://github.com/MINI-FASTCAMPUS5/scheduler-back/assets/68175311/6ea7993d-d437-4fa9-8966-b22f46dc6ed8)
#### 팬
![api명세user](https://github.com/MINI-FASTCAMPUS5/scheduler-back/assets/68175311/3aff0db6-13cf-4161-987e-e60400f28287)
#### 기획사
![api명세admin](https://github.com/MINI-FASTCAMPUS5/scheduler-back/assets/68175311/eacd5d7b-2de4-4f78-951d-48335665381a)


---
## 테이블
```sql
create table user_tb (
     id bigint not null auto_increment,
     created_at datetime not null,
     email varchar(60) not null unique,
     full_name varchar(60) not null,
     latest_login datetime,
     password varchar(60) not null,
     profile_image varchar(255),
     role varchar(255) not null,
     size_of_ticket integer,
     updated_at datetime,
     used_ticket integer,
     primary key (id)
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

```sql
create table login_log_tb (
    id bigint not null auto_increment,
    clientip varchar(255),
    created_at datetime,
    user_agent varchar(255),
    user_id bigint,
    primary key (id),
    foreign key (user_id) references user_tb(id)
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

```sql
create table scheduler_admin_tb (
    id bigint not null auto_increment,
    created_at datetime not null,
    description LONGTEXT,
    image varchar(255),
    schedule_end datetime,
    schedule_start datetime not null,
    title varchar(20),
    updated_at datetime,
    user_id bigint,
    primary key (id),
    foreign key (user_id) references user_tb(id) on update cascade 
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

create table scheduler_user_tb (
   id bigint not null auto_increment,
   created_at datetime not null,
   progress varchar(255),
   schedule_start datetime not null,
   scheduler_admin_id bigint,
   user_id bigint,
   primary key (id),
   foreign key (user_id) references user_tb(id) on update cascade,
   foreign key (scheduler_admin_id) references scheduler_admin_tb(id) on update cascade
) engine=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```
