CREATE TABLE `option` (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          option_text VARCHAR(80) NOT NULL,
                          is_correct BOOLEAN NOT NULL,
                          task_id BIGINT NOT NULL,
                          CONSTRAINT fk_option_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE INDEX idx_option_task ON `option`(task_id);
