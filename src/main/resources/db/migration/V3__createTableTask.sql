CREATE TABLE task (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      statement VARCHAR(255) NOT NULL,
                      `order` INT NOT NULL,
                      type VARCHAR(50) NOT NULL,
                      course_id BIGINT NOT NULL,
                      CONSTRAINT fk_task_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
                      CONSTRAINT uk_task_statement_course UNIQUE (statement, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE INDEX idx_task_course ON task(course_id);
CREATE INDEX idx_task_order ON task(course_id, `order`);
