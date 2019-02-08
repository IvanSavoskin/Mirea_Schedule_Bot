CREATE TABLE IF NOT EXISTS "User"(
	"chatId" integer PRIMARY KEY,
	"chatName" text NOT NULL,
	"groupId" integer NULL
);

CREATE TABLE IF NOT EXISTS "Group"(
	"id" integer PRIMARY KEY,
	"groupName" text NOT NULL,
	"fileName" text NULL
);

CREATE TABLE IF NOT EXISTS "Schedule"(
	"id" integer PRIMARY KEY,
	"classNumber" smallint NOT NULL,
	"subjectId" integer NOT NULL,
	"subjectTypeId" integer NOT NULL,
	"classroomId" integer NOT NULL,
	"dayOfWeek" smallint NOT NULL,
	"numberOfWeek" smallint NOT NULL
);

CREATE TABLE IF NOT EXISTS "Group_Schedule"(
	"scheduleId" integer,
	"groupId" integer,
	PRIMARY KEY ("scheduleId", "groupId")
);

CREATE TABLE IF NOT EXISTS "User_Schedule"(
	"userId" integer,
	"scheduleId" integer,
	PRIMARY KEY ("userId", "scheduleId")
);

CREATE TABLE IF NOT EXISTS "ClassTime"(
	"classNumber" smallint PRIMARY KEY,
	"classStart" text NOT NULL,
	"classStop" text NOT NULL
);

CREATE TABLE IF NOT EXISTS "Classroom"(
	"id" integer PRIMARY KEY,
	"className"text NOT NULL,
	"pic" text NULL
);

CREATE TABLE IF NOT EXISTS "Subject"(
	"id" integer PRIMARY KEY,
	"subjectName" text NOT NULL,
	"teacherId" integer NULL
);

CREATE TABLE IF NOT EXISTS "Teacher"(
	"id" integer PRIMARY KEY,
	"name" text NOT NULL,
	"surname" text NOT NULL,
	"second_name" text NOT NULL,
	"phone_number" text NULL,
	"mail" text NULL
);

CREATE TABLE IF NOT EXISTS "SubjectType"(
	"id" smallint PRIMARY KEY,
	"typeName" text NULL
);

ALTER TABLE "Schedule" DROP CONSTRAINT IF EXISTS "FK_Schedule_Classroom";
ALTER TABLE "Schedule" ADD CONSTRAINT "FK_Schedule_Classroom" FOREIGN KEY("classroomId")
REFERENCES "Classroom" ("id")
ON UPDATE CASCADE
ON DELETE CASCADE;

ALTER TABLE "Schedule" DROP CONSTRAINT IF EXISTS "FK_Schedule_Subject";
ALTER TABLE "Schedule" ADD CONSTRAINT "FK_Schedule_Subject" FOREIGN KEY("subjectId")
REFERENCES "Subject" ("id")
ON UPDATE CASCADE
ON DELETE CASCADE;

ALTER TABLE "Schedule" DROP CONSTRAINT IF EXISTS "FK_Schedule_ClassTime";
ALTER TABLE "Schedule" ADD CONSTRAINT "FK_Schedule_ClassTime" FOREIGN KEY("classNumber")
REFERENCES "ClassTime" ("classNumber")
ON UPDATE CASCADE
ON DELETE CASCADE;

ALTER TABLE "Schedule" DROP CONSTRAINT IF EXISTS "FK_SubjectType_Subject";
ALTER TABLE "Schedule" ADD CONSTRAINT "FK_SubjectType_Subject" FOREIGN KEY("subjectTypeId")
REFERENCES "SubjectType" ("id")
ON UPDATE CASCADE
ON DELETE CASCADE;

ALTER TABLE "Group_Schedule" DROP CONSTRAINT IF EXISTS "FK_Group_Schedule_Class";
ALTER TABLE "Group_Schedule" ADD CONSTRAINT "FK_Group_Schedule_Class" FOREIGN KEY("scheduleId")
REFERENCES "Schedule" ("id")
ON UPDATE CASCADE
ON DELETE CASCADE;

ALTER TABLE "Group_Schedule" DROP CONSTRAINT IF EXISTS "FK_Group_Schedule_Group";
ALTER TABLE "Group_Schedule" ADD CONSTRAINT "FK_Group_Schedule_Group" FOREIGN KEY("groupId")
REFERENCES "Group" ("id")
ON UPDATE CASCADE
ON DELETE CASCADE;

ALTER TABLE "User_Schedule" DROP CONSTRAINT IF EXISTS "FK_User_Schedule_Class";
ALTER TABLE "User_Schedule" ADD CONSTRAINT "FK_User_Schedule_Class" FOREIGN KEY("scheduleId")
REFERENCES "Schedule" ("id")
ON UPDATE CASCADE
ON DELETE CASCADE;

ALTER TABLE "User_Schedule" DROP CONSTRAINT IF EXISTS "FK_User_Schedule_User";
ALTER TABLE "User_Schedule" ADD CONSTRAINT "FK_User_Schedule_User" FOREIGN KEY("userId")
REFERENCES "User" ("chatId")
ON UPDATE CASCADE
ON DELETE CASCADE;

ALTER TABLE "Subject" DROP CONSTRAINT IF EXISTS "FK_Subject_Teacher";
ALTER TABLE "Subject" ADD CONSTRAINT "FK_Subject_Teacher" FOREIGN KEY("teacherId")
REFERENCES "Teacher" ("id")
ON UPDATE SET NULL
ON DELETE SET NULL;

ALTER TABLE "User" DROP CONSTRAINT IF EXISTS "FK_User_Group";
ALTER TABLE "User" ADD CONSTRAINT "FK_User_Group" FOREIGN KEY("groupId")
REFERENCES "Group" ("id")
ON UPDATE SET NULL
ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS "EducationDate"(
	"semesterStartDate" date NULL,
	"testSessionStartDate" date NULL,
	"examSessionStartDate" date NULL,
	"examSessionStopDate" date NULL
);

CREATE TABLE IF NOT EXISTS "FileSum"(
	"fileName" text NULL,
	"md5" text NULL
);

CREATE TABLE IF NOT EXISTS "AdminInfo"(
	"chatId" integer NOT NULL,
	"password" text NOT NULL,
	"login" text NOT NULL
);

MERGE INTO "ClassTime" ("classNumber", "classStart", "classStop") VALUES
	(1, '09:00', '10:30'),
	(2, '10:40', '12:10'),
	(3, '13:00', '14:30'),
	(4, '14:40', '16:10'),
	(5, '16:20', '17:50'),
	(6, '18:00', '19:30'),
	(7, '18:30', '20:00'),
	(8, '20:10', '21:40');

/**INSERT INTO "ClassTime" ("classNumber", "classStart", "classStop") VALUES
	(1, '09:00', '10:30'),
	(2, '10:40', '12:10'),
	(3, '13:00', '14:30'),
	(4, '14:40', '16:10'),
	(5, '16:20', '17:50'),
	(6, '18:00', '19:30'),
	(7, '18:30', '20:00'),
	(8, '20:10', '21:40')
	ON CONFLICT("classNumber") DO NOTHING;*/