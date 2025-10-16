CREATE TABLE IF NOT EXISTS `user` (
    id              INT          AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    hashed_password CHAR(60)     NOT NULL,
    name            VARCHAR(100) NOT NULL,
    surname         VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    birth_date      DATE         NOT NULL,
    sign_up_date    DATE         NOT NULL,
    role_code       INT	    	 NOT NULL,  -- 1 - STUDENT, 2 - ADMIN
    sex_code        INT      	 NOT NULL   -- 0 - NOT_STATED, 1 - MALE, 2 - FEMALE
);

CREATE TABLE IF NOT EXISTS student_profile (
    id            INT     AUTO_INCREMENT PRIMARY KEY,
    user_id       INT     NOT NULL,
    university_id INT     NOT NULL,
    speciality_id INT     NOT NULL,
    study_type    TINYINT NOT NULL,  -- 1 - BACHELOUR, 2 - SPECIALIST, 3 - MASTER, 4 - GRADUATE
    course_num    TINYINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admin_profile (
	id				INT AUTO_INCREMENT PRIMARY KEY,
	user_id			INT	NOT NULL,
	organisation_id	INT	NOT NULL,
	FOREIGN KEY	(user_id) REFERENCES `user`(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS university (
	id			INT			 AUTO_INCREMENT	PRIMARY KEY,
	name		VARCHAR(255) NOT NULL,
	short_name	VARCHAR(255),
	promo		TEXT,
	is_state	BOOLEAN		 NOT NULL DEFAULT 1;
);

CREATE TABLE IF NOT EXISTS speciality (
	id		INT			 AUTO_INCREMENT	PRIMARY KEY,
	code	VARCHAR(12)	 NOT NULL,
	name	VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS speciality_university (
	id			  INT AUTO_INCREMENT PRIMARY KEY,
	speciality_id INT NOT NULL,
	university_id INT NOT NULL
);

CREATE TABLE IF NOT EXISTS organisation (
	id							INT		     AUTO_INCREMENT	PRIMARY KEY,
	name						VARCHAR(255) NOT NULL,
	is_university_subdivision	BOOLEAN		 NOT NULL,
	university_id				INT						-- NULL FOR PARTNERS
);

CREATE TABLE IF NOT EXISTS university_partner (
	id				INT	AUTO_INCREMENT	PRIMARY KEY,
	organisation_id	INT	NOT NULL,
	university_id	INT	NOT NULL
);

CREATE TABLE IF NOT EXISTS vacancy (
	id				INT			 AUTO_INCREMENT	PRIMARY KEY,
	organisation_id	INT			 NOT NULL,
	position		VARCHAR(255) NOT NULL,
	min_salary		INT,
	max_salary		INT,
	shedule			VARCHAR(255),
	description		TEXT
);

CREATE TABLE IF NOT EXISTS vacancy_responce (
	id			INT		AUTO_INCREMENT PRIMARY KEY,
	student_id	INT		NOT NULL,
	vacancy_id	INT		NOT NULL,
	verdit		BOOLEAN
);

CREATE TABLE IF NOT EXISTS vacancy_feedback (
	id				INT		AUTO_INCREMENT PRIMARY KEY,
	organisation_id	INT		NOT NULL,
	student_id		INT		NOT NULL,
	feedback		TEXT,
	reccomendds		BOOLEAN
);