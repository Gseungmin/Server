CREATE DATABASE IF NOT EXISTS `personal` DEFAULT CHARACTER SET utf8mb4;

USE `personal`;

drop table if exists `comment`;
create table `comment` (
    `comment_id` bigint not null,
    `content` varchar(255),
    `member_id` bigint,
    `project_id` bigint,
    primary key (`comment_id`)
) engine=InnoDB DEFAULT CHARSET=utf8;

drop table if exists `hibernate_sequence`;
create table `hibernate_sequence` (
    `next_val` bigint
) engine=InnoDB DEFAULT CHARSET=utf8;

drop table if exists `likes`;
create table `likes` (
    `likes_id` bigint not null,
    `mem_id` bigint,
    `pro_id` bigint,
    `member_id` bigint,
    `project_id` bigint,
    primary key (`likes_id`)
) engine=InnoDB, DEFAULT CHARSET=utf8;

drop table if exists `member`;
create table `member` (
    `member_id` bigint not null,
    `type` varchar(255),
    `email` varchar(255),
    `introduction` varchar(255),
    `nickname` varchar(255),
    `password` varchar(255),
    `profile_image` varchar(255),
    primary key (`member_id`)
) engine=InnoDB, DEFAULT CHARSET=utf8;

drop table if exists `member_roles`;
create table `member_roles` (
    `member_member_id` bigint not null,
    `roles` varchar(255)
) engine=InnoDB, DEFAULT CHARSET=utf8;

drop table if exists `project`;
create table `project` (
    `project_id` bigint not null,
     `category` integer,
     `comment_count` integer,
     `content` varchar(255),
     `image` varchar(255),
     `like_count` integer,
     `link_image` varchar(255),
     `link_title` varchar(255),
     `link_url` varchar(255),
     `sort` integer,
     `title` varchar(255),
     `view_count` integer,
     `member_id` bigint,
     primary key (`project_id`)
) engine=InnoDB, DEFAULT CHARSET=utf8;