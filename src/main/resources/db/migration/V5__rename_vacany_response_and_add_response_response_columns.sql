ALTER TABLE vacancy_responce RENAME TO vacancy_response;
ALTER TABLE vacancy_response RENAME COLUMN verdit TO employer_verdict;
ALTER TABLE vacancy_response ADD COLUMN employer_comment TEXT;
ALTER TABLE vacancy_response ADD COLUMN employer_response TEXT;