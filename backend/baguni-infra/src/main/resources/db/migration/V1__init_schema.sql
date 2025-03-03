create table baguni_db.link
(
    id                bigint auto_increment
        primary key,
    invalidated_at_at datetime(6)  null,
    description       text         null,
    image_url         text         null,
    title             text         null,
    url               varchar(600) not null,
    constraint UK4dycbe6q8trcendnql3b13cuf
        unique (url)
);

create table baguni_db.rss_blog
(
    created_at datetime(6)  not null,
    id         bigint auto_increment
        primary key,
    updated_at datetime(6)  not null,
    blog_name  varchar(255) not null,
    url        varchar(255) not null,
    constraint UKhkgcu0q1xs43reu4pa6xhpt24
        unique (blog_name),
    constraint UKs6orlq8fncv7ps4wpp05o1pv
        unique (url)
);

create table baguni_db.rss_raw_data
(
    created_at      datetime(6)  not null,
    id              bigint auto_increment
        primary key,
    rss_blog_id     bigint       null,
    updated_at      datetime(6)  not null,
    creator         varchar(255) null,
    description     longblob     null,
    guid            varchar(255) null,
    joined_category varchar(255) null,
    published_at    varchar(255) null,
    title           varchar(255) null,
    url             varchar(600) null
);

create table baguni_db.user
(
    created_at         datetime(6)                                    not null,
    id                 bigint auto_increment
        primary key,
    updated_at         datetime(6)                                    not null,
    email              varchar(255)                                   not null,
    nickname           varchar(255)                                   null,
    password           varchar(255)                                   null,
    social_provider_id varchar(255)                                   null,
    tag_order          longblob                                       not null,
    role               enum ('ROLE_ADMIN', 'ROLE_GUEST', 'ROLE_USER') not null,
    social_provider    enum ('GOOGLE', 'KAKAO')                       null
);

create table baguni_db.folder
(
    created_at         datetime(6)                                             not null,
    id                 bigint auto_increment
        primary key,
    parent_folder_id   bigint                                                  null,
    updated_at         datetime(6)                                             not null,
    user_id            bigint                                                  not null,
    child_folder_order longblob                                                not null,
    name               varchar(255)                                            not null,
    pick_order         longblob                                                not null,
    folder_type        enum ('GENERAL', 'RECYCLE_BIN', 'ROOT', 'UNCLASSIFIED') not null,
    constraint FK57g7veis1gp5wn3g0mp0x57pl
        foreign key (parent_folder_id) references baguni_db.folder (id)
            on delete cascade,
    constraint FK5fd2civdi8s832iyrufpk400k
        foreign key (user_id) references baguni_db.user (id)
);

create table baguni_db.pick
(
    created_at       datetime(6)  not null,
    id               bigint auto_increment
        primary key,
    link_id          bigint       not null,
    parent_folder_id bigint       not null,
    updated_at       datetime(6)  not null,
    user_id          bigint       not null,
    tag_order        longblob     not null,
    title            varchar(255) not null,
    constraint FKbilrp2m7mc9ssut5d85loj5d7
        foreign key (user_id) references baguni_db.user (id),
    constraint FKf3o2jbamw9l96i1lwvaytuik7
        foreign key (link_id) references baguni_db.link (id),
    constraint FKhfrafg7f40ym7wgrtp9j45pha
        foreign key (parent_folder_id) references baguni_db.folder (id)
);

create table baguni_db.shared_folder
(
    created_at datetime(6) not null,
    folder_id  bigint      not null,
    updated_at datetime(6) not null,
    user_id    bigint      not null,
    id         binary(16)  not null
        primary key,
    constraint UK5p0agkwypm465pveqn7na9tig
        unique (folder_id),
    constraint FK34v8mqhr9a6rwep0hi9aegr79
        foreign key (user_id) references baguni_db.user (id),
    constraint FK8xepmn10i8pgw3w1rwuffwynp
        foreign key (folder_id) references baguni_db.folder (id)
);

create table baguni_db.tag
(
    color_number int          not null,
    id           bigint auto_increment
        primary key,
    user_id      bigint       not null,
    name         varchar(255) not null,
    constraint UC_TAG_NAME_PER_USER
        unique (user_id, name),
    constraint FKld85w5kr7ky5w4wda3nrdo0p8
        foreign key (user_id) references baguni_db.user (id)
);

create table baguni_db.pick_tag
(
    id      bigint auto_increment
        primary key,
    pick_id bigint not null,
    tag_id  bigint not null,
    constraint UC_PICK_TAG
        unique (pick_id, tag_id),
    constraint FK9e42g0lyb0ss1pjhvdrqqh0a8
        foreign key (tag_id) references baguni_db.tag (id),
    constraint FKcbtnw1dxhgh641h8yjp9nwnav
        foreign key (pick_id) references baguni_db.pick (id)
);

