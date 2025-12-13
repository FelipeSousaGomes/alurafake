CREATE TABLE `options` (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           optionText VARCHAR(80) NOT NULL,
                           isCorrect BOOLEAN NOT NULL,
                           task_id BIGINT NOT NULL,
                           CONSTRAINT fk_options_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

CREATE INDEX idx_options_task ON `options`(task_id);
