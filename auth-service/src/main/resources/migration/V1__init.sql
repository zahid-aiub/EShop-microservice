CREATE TABLE `users`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `first_name` varchar(255),
    `last_name`  varchar(255),
    `email`  varchar(255),
    `username`  varchar(255),
    `password`  varchar(255),
    `role`  varchar(255),
    PRIMARY KEY (`id`)
);