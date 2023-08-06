<div align=center>
    <img src="https://capsule-render.vercel.app/api?type=waving&color=642BF6&height=250&section=header&text=MINI PROJECT 12 BACK-END&fontSize=50&fontColor=ffffff" />
</div>
<div align=center><h1>📚 STACKS</h1>
    <img src="https://img.shields.io/badge/java 11-007396?style=for-the-badge&logo=java&logoColor=white">
    <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
    <img src="https://img.shields.io/badge/spring Security-6DB33F?style=for-the-badge&logo=spring Security&logoColor=white">
    <br>
    <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
    <img src="https://img.shields.io/badge/JPA-58FAD0?style=for-the-badge&logo=JPA&logoColor=white">
    <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
    <br>
    <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
    <img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=for-the-badge&logo=IntelliJ IDEA&logoColor=white">
    <h2>💬 Communication</h2>
    <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=Slack&logoColor=white">
    <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">
    <img src="https://img.shields.io/badge/Zoom-2D8CFF?style=for-the-badge&logo=Zoom&logoColor=white">
    <h1>👨‍👩‍👧‍👦 팀원 역할</h1>
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
                <td width="180">
                    게시판 조회,<br />스케쥴러 리팩토링,<br />사용자 예외 페이지,<br />화면 구현<br />페이징, 검색 기능
                </td>
                <td width="180">
                    gradle multi module 적용,<br />관리자 api,<br />세션 기반 로그인,<br />예외처리 등<br />관리자<br />api 및 화면 일체
                </td>
                <td width="180">
                    댓글 기능 구현,<br />게시판 CRUD 구현,<br />게시판 상세보기 구현,<br />게시판 신고 기능 구현,<br />로컬 저장소를 통한 이미지 관리,<br />깃허브 관리
                </td>
            </tr>
        </tbody>
    </table>
</div>

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
    foreign key (user_id) references user(id)
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