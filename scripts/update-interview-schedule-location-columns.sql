ALTER TABLE interview_schedule
ADD COLUMN location_type VARCHAR(20) NOT NULL DEFAULT 'CLUB_ROOM',
ADD COLUMN location_detail TEXT NULL;
