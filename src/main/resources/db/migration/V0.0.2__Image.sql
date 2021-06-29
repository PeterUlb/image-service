CREATE TABLE image
(
    id           bigserial                NOT NULL PRIMARY KEY,
    external_key character varying(20)    NOT NULL,
    account_id   uuid                     NOT NULL,
    title        text,
    description  text,
    filename     character varying(255)   NOT NULL,
    extension    character varying(10),
    mime_type    character varying(50),
    size         bigint,
    width        bigint,
    height       bigint,
    privacy      character varying(50)    NOT NULL,
    image_status character varying(20)    NOT NULL DEFAULT 'REQUESTED',
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT image_account_id_external_key_uk UNIQUE (account_id, external_key)
);

CREATE TRIGGER set_update_timestamp
    BEFORE UPDATE
    ON image
    FOR EACH ROW
    EXECUTE PROCEDURE set_update_timestamp();

CREATE TABLE image_tag
(
    id  bigserial                     NOT NULL PRIMARY KEY,
    tag character varying(255) UNIQUE NOT NULL
);

CREATE TABLE image_image_tag
(
    image_id     BIGINT NOT NULL,
    image_tag_id BIGINT NOT NULL,
    PRIMARY KEY (image_id, image_tag_id),
    CONSTRAINT image_image_tag_fk1 FOREIGN KEY (image_id) REFERENCES image (id),
    CONSTRAINT image_image_tag_fk2 FOREIGN KEY (image_tag_id) REFERENCES image_tag (id)
);

CREATE INDEX image_imageTag_tag_id_idx ON image_image_tag (image_tag_id);

CREATE TABLE image_gallery
(
    id           bigserial                NOT NULL PRIMARY KEY,
    external_key character varying(20)    NOT NULL,
    account_id   bigint                   NOT NULL,
    type         character varying(50)    NOT NULL,
    title        text,
    description  text,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT imageGallery_accountId_externalKey_uk UNIQUE (account_id, external_key)
);

CREATE TRIGGER set_update_timestamp
    BEFORE UPDATE
    ON image_gallery
    FOR EACH ROW
    EXECUTE PROCEDURE set_update_timestamp();

CREATE TABLE image_gallery_image
(
    image_gallery_id BIGINT NOT NULL,
    image_id         BIGINT NOT NULL,
    PRIMARY KEY (image_gallery_id, image_id),
    CONSTRAINT image_gallery_image_fk1 FOREIGN KEY (image_gallery_id) REFERENCES image_gallery (id),
    CONSTRAINT image_gallery_image_fk2 FOREIGN KEY (image_id) REFERENCES image (id)
);