ALTER TABLE applicant_sync_log
    ADD CONSTRAINT uk_applicant_sync_log_semester_form_response
        UNIQUE (application_semester_id, form_id, response_id);
