-- SET NAMES utf8mb4
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Create Database
CREATE DATABASE IF NOT EXISTS school_system CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE school_system;

-- (1) 课程介绍信息表 test
CREATE TABLE `test` (
    `tid` INT AUTO_INCREMENT PRIMARY KEY,
    `ttitle` VARCHAR(255) NOT NULL COMMENT '标题',
    `tcontent` TEXT COMMENT '课程信息描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (2) 班级信息表 classes
CREATE TABLE `classes` (
    `cid` INT AUTO_INCREMENT PRIMARY KEY,
    `cname` VARCHAR(100) NOT NULL COMMENT '名称',
    `cdescript` VARCHAR(255) COMMENT '班级描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (3) 教师用户信息表 teacher
CREATE TABLE `teacher` (
    `tid` INT AUTO_INCREMENT PRIMARY KEY,
    `tname` VARCHAR(50) NOT NULL COMMENT '姓名',
    `tpassword` VARCHAR(100) NOT NULL COMMENT '密码',
    `tdate` DATE COMMENT '出生日期',
    `tpic` VARCHAR(255) COMMENT '教师照片',
    `tdescript` TEXT COMMENT '教师描述',
    `tno` VARCHAR(50) UNIQUE NOT NULL COMMENT '教师编号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (4) 学生用户信息表 user
CREATE TABLE `user` (
    `uid` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL COMMENT '姓名',
    `userpassword` VARCHAR(100) NOT NULL COMMENT '密码',
    `usersex` VARCHAR(10) COMMENT '性别',
    `userno` VARCHAR(50) UNIQUE NOT NULL COMMENT '学生编号',
    `userdescript` TEXT COMMENT '学生描述',
    `class_id` INT COMMENT '班级id',
    `upic` VARCHAR(255) COMMENT '学生照片',
    `youxiuok` VARCHAR(10) DEFAULT '否' COMMENT '是否优先/优秀',
    `checkedok` VARCHAR(10) DEFAULT '待审核' COMMENT '是否审核通过',
    `classname` VARCHAR(100) COMMENT '班级名称',
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (5) 教学内容信息表 course
CREATE TABLE `course` (
    `cid` INT AUTO_INCREMENT PRIMARY KEY,
    `ctitle` VARCHAR(255) NOT NULL COMMENT '教学内容名称',
    `ccontent` TEXT COMMENT '教学内容',
    `efile` VARCHAR(255) COMMENT '相关文件'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (6) 实验内容信息表 experiment
CREATE TABLE `experiment` (
    `eid` INT AUTO_INCREMENT PRIMARY KEY,
    `etitle` VARCHAR(255) NOT NULL COMMENT '实验内容名称',
    `econtent` TEXT COMMENT '实验内容',
    `efile` VARCHAR(255) COMMENT '相关文件'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (7) 互动交流信息表 interaction
CREATE TABLE `interaction` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) COMMENT '名字',
    `comask` TEXT COMMENT '提问内容',
    `asktime` DATETIME COMMENT '提问时间',
    `replname` VARCHAR(50) COMMENT '回答者姓名',
    `comrepl` TEXT COMMENT '回答内容',
    `repltime` DATETIME COMMENT '回答时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (8) 技术动态信息表 news
CREATE TABLE `news` (
    `nid` INT AUTO_INCREMENT PRIMARY KEY,
    `newstitle` VARCHAR(255) NOT NULL COMMENT '标题',
    `newscontent` TEXT COMMENT '内容',
    `newsdate` DATETIME COMMENT '时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (9) 试卷表 exam
CREATE TABLE `exam` (
    `exam_id` INT AUTO_INCREMENT PRIMARY KEY,
    `exam_title` VARCHAR(255) NOT NULL COMMENT '试卷标题',
    `course_id` INT COMMENT '关联的课程介绍ID(test表tid)',
    `course_name` VARCHAR(255) COMMENT '关联课程名称',
    `duration_minutes` INT NOT NULL DEFAULT 60 COMMENT '限时时长(分钟)',
    `pass_score` DECIMAL(5,2) NOT NULL DEFAULT 60.00 COMMENT '及格线',
    `total_score` DECIMAL(6,2) NOT NULL DEFAULT 100.00 COMMENT '总分',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿/PUBLISHED已发布/CLOSED已截止',
    `max_attempts` INT NOT NULL DEFAULT 1 COMMENT '每位学生最大作答次数',
    `scoring_rule` VARCHAR(20) NOT NULL DEFAULT 'ALL_OR_NOTHING' COMMENT '多选题判分规则：ALL_OR_NOTHING全对得分/PROPORTIONAL按比例得分',
    `start_time` DATETIME COMMENT '生效开始时间',
    `end_time` DATETIME COMMENT '截止时间',
    `created_by` INT COMMENT '创建教师ID',
    `created_by_name` VARCHAR(50) COMMENT '创建教师姓名',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- (10) 试卷题目表 exam_question
CREATE TABLE `exam_question` (
    `question_id` INT AUTO_INCREMENT PRIMARY KEY,
    `exam_id` INT NOT NULL COMMENT '所属试卷ID',
    `question_type` VARCHAR(20) NOT NULL DEFAULT 'SINGLE' COMMENT '题目类型：SINGLE单选/MULTIPLE多选',
    `question_text` TEXT NOT NULL COMMENT '题干',
    `score` DECIMAL(5,2) NOT NULL DEFAULT 10.00 COMMENT '分值',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `analysis` TEXT COMMENT '题目解析',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_exam_id` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目表';

-- (11) 题目选项表 exam_option
CREATE TABLE `exam_option` (
    `option_id` INT AUTO_INCREMENT PRIMARY KEY,
    `question_id` INT NOT NULL COMMENT '所属题目ID',
    `option_label` VARCHAR(10) NOT NULL COMMENT '选项标签(A/B/C/D等)',
    `option_text` TEXT NOT NULL COMMENT '选项内容',
    `is_correct` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为正确答案：0否/1是',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目选项表';

-- (12) 作答记录表 exam_attempt
CREATE TABLE `exam_attempt` (
    `attempt_id` INT AUTO_INCREMENT PRIMARY KEY,
    `exam_id` INT NOT NULL COMMENT '试卷ID',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `student_name` VARCHAR(50) COMMENT '学生姓名',
    `student_no` VARCHAR(50) COMMENT '学号',
    `attempt_no` INT NOT NULL DEFAULT 1 COMMENT '第几次作答',
    `score` DECIMAL(6,2) COMMENT '得分',
    `total_score` DECIMAL(6,2) COMMENT '试卷总分(快照)',
    `time_spent_seconds` INT COMMENT '用时(秒)',
    `is_submitted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已提交：0否/1是',
    `is_timeout` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否超时：0否/1是',
    `start_time` DATETIME COMMENT '开始答题时间',
    `submit_time` DATETIME COMMENT '提交时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_exam_student` (`exam_id`, `student_id`),
    INDEX `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作答记录表';

-- (13) 答题详情表 exam_answer
CREATE TABLE `exam_answer` (
    `answer_id` INT AUTO_INCREMENT PRIMARY KEY,
    `attempt_id` INT NOT NULL COMMENT '作答记录ID',
    `question_id` INT NOT NULL COMMENT '题目ID',
    `question_text` TEXT COMMENT '题干(快照)',
    `question_type` VARCHAR(20) COMMENT '题目类型(快照)',
    `score` DECIMAL(5,2) COMMENT '题目分值(快照)',
    `student_answers` VARCHAR(255) COMMENT '学生答案(逗号分隔，如A,C)',
    `correct_answers` VARCHAR(255) COMMENT '正确答案(快照，逗号分隔)',
    `option_snapshot` TEXT COMMENT '选项内容快照(JSON)',
    `analysis` TEXT COMMENT '题目解析(快照)',
    `is_correct` TINYINT(1) COMMENT '是否正确：0否/1是',
    `actual_score` DECIMAL(5,2) COMMENT '实际得分',
    INDEX `idx_attempt_id` (`attempt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题详情表';

-- (14) 公告表 announcement
CREATE TABLE `announcement` (
    `announcement_id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL COMMENT '公告标题',
    `content` TEXT COMMENT '公告正文(富文本/纯文本)',
    `content_type` VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '内容类型：TEXT纯文本/HTML富文本',
    `target_type` VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '目标范围：ALL全体/SPECIFIED指定班级',
    `is_pinned` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶：0否/1是',
    `pin_order` INT NOT NULL DEFAULT 0 COMMENT '置顶排序(置顶公告内部排序)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿/PUBLISHED已发布/REVOKED已撤回',
    `effective_time` DATETIME COMMENT '生效时间',
    `expire_time` DATETIME COMMENT '失效时间',
    `created_by` INT COMMENT '创建教师ID',
    `created_by_name` VARCHAR(50) COMMENT '创建教师姓名',
    `published_at` DATETIME COMMENT '发布时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_published` (`status`, `published_at`),
    INDEX `idx_pinned` (`is_pinned`, `pin_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- (15) 公告-目标班级关联表 announcement_class
CREATE TABLE `announcement_class` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `announcement_id` INT NOT NULL COMMENT '公告ID',
    `class_id` INT NOT NULL COMMENT '班级ID',
    `class_name` VARCHAR(100) COMMENT '班级名称(冗余)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_announcement_class` (`announcement_id`, `class_id`),
    INDEX `idx_announcement_id` (`announcement_id`),
    INDEX `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告目标班级关联表';

-- (16) 公告-已读记录表 announcement_read
CREATE TABLE `announcement_read` (
    `read_id` INT AUTO_INCREMENT PRIMARY KEY,
    `announcement_id` INT NOT NULL COMMENT '公告ID',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `student_name` VARCHAR(50) COMMENT '学生姓名(冗余)',
    `read_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    UNIQUE KEY `uk_announcement_student` (`announcement_id`, `student_id`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_announcement_id` (`announcement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录表';

-- Seeding Data (30 records per table)

-- Classes
INSERT INTO `classes` (cname, cdescript) VALUES 
('计算机2101', '计算机科学与技术班'), ('计算机2102', '计算机科学与技术班'), ('软件2101', '软件工程实验班'), ('软件2102', '软件工程普通班'), ('网络2101', '网络工程班'),
('大数据2101', '大数据技术班'), ('AI2101', '人工智能实验班'), ('物联网2101', '物联网工程班'), ('安防2101', '信息安全班'), ('通信2101', '通信工程班'),
('自动化2101', '自动化技术班'), ('电子2101', '电子信息班'), ('机械2101', '机械制造班'), ('土木2101', '土木工程班'), ('建筑2101', '建筑设计班'),
('管理2101', '工商管理班'), ('会计2101', '财务会计班'), ('金融2101', '金融学班'), ('法律2101', '法学班'), ('外语2101', '英语专业班'),
('数学2101', '数学应用班'), ('物理2101', '物理实验班'), ('化学2101', '应用化学班'), ('生物2101', '生物工程班'), ('艺术2101', '美学设计班'),
('体育2101', '体育教育班'), ('音乐2101', '音乐表演班'), ('舞蹈2101', '舞蹈编导班'), ('表演2101', '戏剧影视班'), ('新闻2101', '新闻传播班');

-- Teachers
INSERT INTO `teacher` (tname, tpassword, tdate, tpic, tdescript, tno) VALUES 
('张老师', '123456', '1980-05-12', 't1.jpg', '高级讲师，擅长Java开发', 'T001'), 
('李老师', '123456', '1982-08-20', 't2.jpg', '副教授，专研数据库系统', 'T002'),
('王老师', '123456', '1975-03-15', 't3.jpg', '资深专家，区块链方向', 'T003'),
('赵老师', '123456', '1988-11-22', 't4.jpg', '博士，人工智能专家', 'T004'),
('孙老师', '123456', '1983-02-10', 't5.jpg', '软件工程师，实战经验丰富', 'T005'),
('周老师', '123456', '1979-06-30', 't6.jpg', '网络安全顾问', 'T006'),
('吴老师', '123456', '1985-09-05', 't7.jpg', '移动开发专家', 'T007'),
('郑老师', '123456', '1981-12-12', 't8.jpg', '由于云计算领域研究', 'T008'),
('冯老师', '123456', '1977-04-18', 't9.jpg', '计算机图形学专家', 'T009'),
('陈老师', '123456', '1984-07-25', 't10.jpg', '编译原理权威', 'T010'),
('褚老师', '123456', '1986-10-01', 't11.jpg', '操作系统研究员', 'T011'),
('卫老师', '123456', '1980-01-20', 't12.jpg', '嵌入式系统专家', 'T012'),
('蒋老师', '123456', '1978-05-15', 't13.jpg', '分布式计算专家', 'T013'),
('沈老师', '123456', '1982-03-03', 't14.jpg', '数据挖掘专家', 'T014'),
('韩老师', '123456', '1989-08-08', 't15.jpg', '机器学习研究员', 'T015'),
('杨老师', '123456', '1983-12-25', 't16.jpg', '计算机视觉专家', 'T016'),
('朱老师', '123456', '1987-06-12', 't17.jpg', 'NLP自然语言处理专家', 'T017'),
('秦老师', '123456', '1981-09-30', 't18.jpg', '信息检索专家', 'T018'),
('尤老师', '123456', '1976-11-11', 't19.jpg', '人机交互专家', 'T019'),
('许老师', '123456', '1985-02-14', 't20.jpg', '软件测试专家', 'T020'),
('何老师', '123456', '1980-04-01', 't21.jpg', '项目管理专家', 'T021'),
('吕老师', '123456', '1988-07-07', 't22.jpg', '敏捷开发教练', 'T022'),
('施老师', '123456', '1982-10-10', 't23.jpg', 'DevOps架构师', 'T023'),
('张三老师', '123456', '1984-05-20', 't24.jpg', '全栈开发工程师', 'T024'),
('孔老师', '123456', '1979-08-15', 't25.jpg', '系统架构师', 'T025'),
('曹老师', '123456', '1981-01-05', 't26.jpg', '微服务专家', 'T026'),
('严老师', '123456', '1983-09-18', 't27.jpg', '大数据分析师', 'T027'),
('华老师', '123456', '1986-12-30', 't28.jpg', '前端高级开发', 'T028'),
('金老师', '123456', '1980-02-28', 't29.jpg', 'UI/UX设计师', 'T029'),
('魏老师', '123456', '1987-04-12', 't30.jpg', '交互设计专家', 'T030');

-- Course Intro (test table)
INSERT INTO `test` (ttitle, tcontent) VALUES 
('Java程序设计', '学习Java基础语法、面向对象编程、异常处理及多线程技术。'),
('数据库原理', '掌握SQL语言、数据库设计范式、事务管理及索引优化。'),
('计算机网络', '深入理解OSI模型、TCP/IP协议族及网络应用开发。'),
('数据结构', '学习链表、树、图等基础结构及排序、查找算法。'),
('操作系统', '探讨进程管理、内存分配、文件系统及设备管理。'),
('软件工程', '学习生命周期模型、需求分析、设计模式及软件测试。'),
('离散数学', '计算机科学的逻辑基础，包括集合、图论、逻辑。'),
('人工智能导论', '探索机器学习、神经网络、深度学习等前沿技术。'),
('算法设计与分析', '掌握贪心、动态规划、回溯等复杂算法设计思路。'),
('计算机组成原理', '理解硬件构造、指令系统、中央处理器及存储架构。'),
('Web开发基础', 'HTML5, CSS3, JavaScript基础及网页布局实现。'),
('Python数据分析', '利用Python及NumPy, Pandas等库进行数据透视。'),
('区块链技术', '共识算法、智能合约、加密货币及去中心化应用。'),
('移动应用开发', 'Android或iOS应用架构设计与生命周期管理。'),
('云计算架构', '虚拟化技术、Docker容器、K8s编排及云原生应用。'),
('编译原理', '词法分析、语法分析、语义分析及代码生成过程。'),
('嵌入式系统', 'ARM架构、实时操作系统及其在物联网中的应用。'),
('网络安全', '加密解密、漏洞扫描、防火墙配置及安全协议。'),
('人机交互', '用户体验设计、交互模型及设计评价准则。'),
('软件测试', '黑盒测试、白盒测试、回归测试及自动化工具使用。'),
('分布式系统', '分布式一致性、RPC调用、消息队列及负载均衡。'),
('机器学习', '监督学习、非监督学习、强化学习及其算法实现。'),
('大数据技术', 'Hadoop, Spark生态圈及非关系型数据库应用。'),
('计算机视觉', '图像处理基础、特征检测、物体识别及OpenCV。'),
('自然语言处理', '文本分类、情感分析、机器翻译及预训练模型。'),
('信号与系统', '连续与离散信号的频域分析及系统稳定性研究。'),
('数字电路', '逻辑门控制、时序电路、触发器及FPGA基础。'),
('信息检索', '搜索引擎原理、倒排索引、TF-IDF及排序算法。'),
('多媒体技术', '视音频压缩、流媒体传输及交互式媒体制作。'),
('职业素质与规划', 'IT行业现状分析、面试技巧及职业道德准则。');

-- Users (Students)
INSERT INTO `user` (username, userpassword, usersex, userno, userdescript, class_id, upic, youxiuok, checkedok, classname) VALUES 
('小明', '123456', '男', 'S2021001', '勤奋好学的学生', 1, 's1.jpg', '是', '已通过', '计算机2101'),
('小红', '123456', '女', 'S2021002', '热衷于前端设计', 1, 's2.jpg', '否', '已通过', '计算机2101'),
('小王', '123456', '男', 'S2021003', '算法竞赛选手', 2, 's3.jpg', '是', '已通过', '计算机2102'),
('小李', '123456', '女', 'S2021004', '数学天才', 2, 's4.jpg', '否', '待审核', '计算机2102'),
('小赵', '123456', '男', 'S2021005', '喜欢开源社区', 3, 's5.jpg', '否', '已通过', '软件2101'),
('小孙', '123456', '女', 'S2021006', '追求极致交互', 3, 's6.jpg', '是', '已通过', '软件2101'),
('小周', '123456', '男', 'S2021007', '网络攻防专家', 4, 's7.jpg', '否', '已通过', '软件2102'),
('小吴', '123456', '女', 'S2021008', '擅长文档编写', 4, 's8.jpg', '否', '待审核', '软件2102'),
('小郑', '123456', '男', 'S2021009', '对硬件感兴趣', 5, 's9.jpg', '否', '已通过', '网络2101'),
('小冯', '123456', '女', 'S2021010', '热爱生活', 5, 's10.jpg', '否', '已通过', '网络2101'),
('小陈', '123456', '男', 'S2021011', '音乐发烧友', 6, 's11.jpg', '否', '已通过', '大数据2101'),
('小褚', '123456', '女', 'S2021012', '艺术特长生', 6, 's12.jpg', '否', '已通过', '大数据2101'),
('小卫', '123456', '男', 'S2021013', '体育健将', 7, 's13.jpg', '否', '已通过', 'AI2101'),
('小蒋', '123456', '女', 'S2021014', '深度学习迷', 7, 's14.jpg', '是', '已通过', 'AI2101'),
('小沈', '123456', '男', 'S2021015', '未来程序员', 8, 's15.jpg', '否', '已通过', '物联网2101'),
('小韩', '123456', '女', 'S2021016', '精通多种门语言', 8, 's16.jpg', '否', '待审核', '物联网2101'),
('小杨', '123456', '男', 'S2021017', '喜欢摄影', 9, 's17.jpg', '否', '已通过', '安防2101'),
('小朱', '123456', '女', 'S2021018', '擅长交流', 9, 's18.jpg', '否', '已通过', '安防2101'),
('小秦', '123456', '男', 'S2021019', '逻辑思维强', 10, 's19.jpg', '否', '已通过', '通信2101'),
('小尤', '123456', '女', 'S2021020', '英语达人', 10, 's20.jpg', '否', '已通过', '通信2101'),
('小许', '123456', '男', 'S2021021', '喜欢旅行', 11, 's21.jpg', '否', '已通过', '自动化2101'),
('小何', '123456', '女', 'S2021022', '绘画专家', 11, 's22.jpg', '否', '已通过', '自动化2101'),
('小吕', '123456', '男', 'S2021023', '科幻迷', 12, 's23.jpg', '否', '已通过', '电子2101'),
('小施', '123456', '女', 'S2021024', '美食家', 12, 's24.jpg', '否', '待审核', '电子2101'),
('小张', '123456', '男', 'S2021025', '热于助人', 13, 's25.jpg', '否', '已通过', '机械2101'),
('小孔', '123456', '女', 'S2021026', '沉稳冷静', 13, 's26.jpg', '否', '已通过', '机械2101'),
('小曹', '123456', '男', 'S2021027', '博览群书', 14, 's27.jpg', '否', '已通过', '土木2101'),
('小严', '123456', '女', 'S2021028', '细心严谨', 14, 's28.jpg', '否', '已通过', '土木2101'),
('小华', '123456', '男', 'S2021029', '自信大方', 15, 's29.jpg', '否', '已通过', '建筑2101'),
('小金', '123456', '女', 'S2021030', '活泼开朗', 15, 's30.jpg', '否', '待审核', '建筑2101');

-- Courses (Teaching Content)
INSERT INTO `course` (ctitle, ccontent, efile) VALUES 
('Java第1章：基础简介', 'Java的历史与开发环境搭建。', 'chapter1.pdf'),
('Java第2章：数据类型', '基本数据类型与引用数据类型详解。', 'chapter2.pdf'),
('Java第3章：控制流', 'if-else, switch, loop控制结构。', 'chapter3.pdf'),
('Java第4章：类与对象', '封装、继承、多态三大特性。', 'chapter4.pdf'),
('Java第5章：常用类', 'String, Date, Math等工具类。', 'chapter5.pdf'),
('Java第6章：集合框架', 'List, Set, Map接口及其实现类。', 'chapter6.pdf'),
('Java第7章：IO流', '字节流与字符流的操作。', 'chapter7.pdf'),
('Java第8章：多线程', '线程生命周期与同步机制。', 'chapter8.pdf'),
('数据库第1章：绪论', '数据库系统架构。', 'db1.pdf'),
('数据库第2章：关系模型', '关系代数与完整性约束。', 'db2.pdf'),
('数据库第3章：SQL语言', '增删改查语法基础。', 'db3.pdf'),
('数据库第4章：规范化', '第一、二、三范式推导。', 'db4.pdf'),
('网络第1章：概述', '分层协议模型简介。', 'net1.pdf'),
('网络第2章：物理层', '传输介质与信道技术。', 'net2.pdf'),
('网络第3章：链路层', '以太网协议与MAC地址。', 'net3.pdf'),
('网络第4章：网络层', 'IP地址划分与路由协议。', 'net4.pdf'),
('OS第1章：概述', '操作系统的功能与分类。', 'os1.pdf'),
('OS第2章：进程管理', '进程调度算法实现。', 'os2.pdf'),
('OS第3章：存储管理', '分页与分段技术。', 'os3.pdf'),
('OS第4章：文件管理', '目录结构与存储空间分配。', 'os4.pdf'),
('新一代Web技术', 'React与Vue框架对比。', 'web_new.pdf'),
('移动开发前沿', 'Flutter跨平台开发实践。', 'mobile_edge.pdf'),
('AI与生活', '人工智能在日常生活中的应用。', 'ai_life.pdf'),
('网络安全攻防', '常见Web攻击防御策略。', 'security_lab.pdf'),
('性能优化实战', '后端接口响应时间优化。', 'perf_opt.pdf'),
('微服务实战', 'Spring Cloud微服务治理。', 'microservice.pdf'),
('大数据挖掘', '用户画像系统构建。', 'bigdata_mining.pdf'),
('容器化技术', 'Docker镜像打包流程。', 'docker_pkg.pdf'),
('算法高级进阶', '图论最短路径算法分析。', 'algo_adv.pdf'),
('软件架构设计', '整齐架构与领域驱动设计。', 'arch_design.pdf');

-- Experiments
INSERT INTO `experiment` (etitle, econtent, efile) VALUES 
('Java实验1：环境搭建', '安装JDK并编写HelloWorld。', 'exp1.zip'),
('Java实验2：控制结构', '实现九九乘法表。', 'exp2.zip'),
('Java实验3：面向对象', '设计一个简单的图书管理系统类模型。', 'exp3.zip'),
('Java实验4：异常处理', '编写自定义异常并进行捕获。', 'exp4.zip'),
('Java实验5：文件读写', '通过IO流读取本地文档内容。', 'exp5.zip'),
('Java实验6：网络编程', '实现简单的Socket聊天程序。', 'exp6.zip'),
('数据库实验1：建表', '使用SQL语句创建学生成绩表。', 'dbexp1.zip'),
('数据库实验2：查询', '进行复杂的多表连接查询练习。', 'dbexp2.zip'),
('数据库实验3：视图', '创建视图并优化查询流程。', 'dbexp3.zip'),
('数据库实验4：触发器', '实现审计日志的自动记录。', 'dbexp4.zip'),
('网络实验1：抓包分析', '使用Wireshark抓取HTTP请求。', 'netexp1.zip'),
('网络实验2：路由配置', '静态路由与RIP协议配置。', 'netexp2.zip'),
('网络实验3：子网划分', '规划企业内网IP段。', 'netexp3.zip'),
('OS实验1：进程调度', '模拟FCFS调度算法过程。', 'osexp1.zip'),
('OS实验2：内存分配', '模拟首次适应分配算法。', 'osexp2.zip'),
('OS实验3：文件系统', '模拟FAT文件系统寻址。', 'osexp3.zip'),
('算法实验1：分治法', '实现大整数乘法。', 'algoexp1.zip'),
('算法实验2：动态规划', '解决01背包问题。', 'algoexp2.zip'),
('算法实验3：贪心算法', '实现赫夫曼编码。', 'algoexp3.zip'),
('AI实验1：逻辑回归', '通过Python实现手写数字识别。', 'aiexp1.zip'),
('AI实验2：神经网络', '搭建简单的卷积神经网络模型。', 'aiexp2.zip'),
('Web实验1：静态页面', '制作个人简介网页。', 'webexp1.zip'),
('Web实验2：动态交互', '使用JS实现轮播图效果。', 'webexp2.zip'),
('嵌入式实验1：LED', '编写代码控制开发板灯光。', 'embexp1.zip'),
('安防实验1：防火墙', '配置iptables规则过滤流量。', 'secu_exp1.zip'),
('系统综合实验1', '整合前后端开发。', 'comp1.zip'),
('系统综合实验2', '测试与性能调优。', 'comp2.zip'),
('系统综合实验3', '编写技术报告。', 'comp3.zip'),
('职业素质实验', '模拟面试。', 'prof_exp1.zip'),
('创新实验课题', '自主选定课题开发。', 'innov_exp1.zip');

-- Interactions
INSERT INTO `interaction` (`name`, `comask`, `asktime`, `replname`, `comrepl`, `repltime`) VALUES 
('小明', '老师，Java中的接口和抽象类有什么区别？', '2026-02-20 10:00:00', '张老师', '接口主要是行为的契约，而抽象类是模板的复用。', '2026-02-20 11:00:00'),
('小红', 'CSS中的Flex布局怎么水平居中？', '2026-02-20 14:00:00', '华老师', '设置justify-content: center; 即可。', '2026-02-20 15:00:00'),
('小王', '数据库索引失效的情况有哪些？', '2026-02-21 09:00:00', '李老师', '比如使用了like在前缀通配符，或者违反了最左匹配原则。', '2026-02-21 10:30:00'),
('小李', 'Spring Boot如何配置跨域？', '2026-02-21 16:00:00', '陈老师', '可以使用WebMvcConfigurer或者@CrossOrigin注解。', '2026-02-21 17:00:00'),
('小赵', 'Docker Compose的作用是什么？', '2026-02-22 08:30:00', '施老师', '用于定义和运行多容器Docker应用程序的工具。', '2026-02-22 09:15:00'),
('小孙', '算法时间复杂度怎么估算？', '2026-02-22 13:00:00', '周老师', '通常看最深层循环的执行次数。', '2026-02-22 14:00:00'),
('小周', 'Linux查看端口占用的命令是什么？', '2026-02-23 10:10:00', '褚老师', 'lsof -i :port 或 netstat -tunlp。', '2026-02-23 11:00:00'),
('小张', 'Vue3相比Vue2最大的变化是？', '2026-02-23 15:00:00', '华老师', '引入了Composition API和更快的渲染性能。', '2026-02-23 16:00:00'),
('小明', 'TCP三次握手的过程？', '2026-02-24 09:20:00', '卫老师', 'SYN -> SYN-ACK -> ACK。', '2026-02-24 10:00:00'),
('小红', '什么是死锁？怎么避免？', '2026-02-24 11:00:00', '蒋老师', '资源互相等待形成的环路；可以通过有序申请资源来避免。', '2026-02-24 12:00:00'),
('小明', '提问11', '2026-02-24 12:00:00', '陈老师', '回答11', '2026-02-24 13:00:00'),
('小王', '提问12', '2026-02-24 13:00:00', '陈老师', '回答12', '2026-02-24 14:00:00'),
('小李', '提问13', '2026-02-24 14:00:00', '陈老师', '回答13', '2026-02-24 15:00:00'),
('小赵', '提问14', '2026-02-24 15:00:00', '陈老师', '回答14', '2026-02-24 16:00:00'),
('小孙', '提问15', '2026-02-24 16:00:00', '陈老师', '回答15', '2026-02-24 17:00:00'),
('小周', '提问16', '2026-02-24 17:00:00', '陈老师', '回答16', '2026-02-24 18:00:00'),
('小吴', '提问17', '2026-02-25 09:00:00', NULL, NULL, NULL),
('小郑', '提问18', '2026-02-25 10:00:00', NULL, NULL, NULL),
('小冯', '提问19', '2026-02-25 11:00:00', NULL, NULL, NULL),
('小陈', '提问20', '2026-02-25 12:00:00', NULL, NULL, NULL),
('小褚', '提问21', '2026-02-25 13:00:00', NULL, NULL, NULL),
('小卫', '提问22', '2026-02-25 14:00:00', NULL, NULL, NULL),
('小蒋', '提问23', '2026-02-25 15:00:00', NULL, NULL, NULL),
('小沈', '提问24', '2026-02-25 16:00:00', NULL, NULL, NULL),
('小韩', '提问25', '2026-02-25 17:00:00', NULL, NULL, NULL),
('小杨', '提问26', '2026-02-26 09:00:00', NULL, NULL, NULL),
('小朱', '提问27', '2026-02-26 10:00:00', NULL, NULL, NULL),
('小秦', '提问28', '2026-02-26 11:00:00', NULL, NULL, NULL),
('小尤', '提问29', '2026-02-26 12:00:00', NULL, NULL, NULL),
('小许', '提问30', '2026-02-26 13:00:00', NULL, NULL, NULL);

-- News
INSERT INTO `news` (newstitle, newscontent, newsdate) VALUES 
('Java 21 正式发布', 'Java 21 带来了虚拟线程等重大特性。', '2026-02-01 10:00:00'),
('Spring Boot 3.2 特性概览', '支持虚拟线程和GraalVM。', '2026-02-02 11:00:00'),
('MySQL 8.4 版本更新', '安全性与性能的进一步提升。', '2026-02-03 12:00:00'),
('Bootstrap 6 开发动态', '全新的排版系统与组件。', '2026-02-04 13:00:00'),
('人工智能助力教育转型', 'AI在个性化教学中的应用。', '2026-02-05 14:00:00'),
('云计算行业趋势报告', '云原生已成为行业标准。', '2026-02-06 15:00:00'),
('区块链技术赋能供应链', '提高透明度与可追溯性。', '2026-02-07 16:00:00'),
('网络安全法新规解读', '加强数据保护与合规性。', '2026-02-08 17:00:00'),
('鸿蒙系统开发者大会', '共建全场景智慧生态。', '2026-02-09 18:00:00'),
('2026 IT 行业人才需求', '全栈开发与AI人才紧缺。', '2026-02-10 19:00:00'),
('技术前沿11', '内容详情11', '2026-02-11 08:00:00'),
('技术前沿12', '内容详情12', '2026-02-12 08:00:00'),
('技术前沿13', '内容详情13', '2026-02-13 08:00:00'),
('技术前沿14', '内容详情14', '2026-02-14 08:00:00'),
('技术前沿15', '内容详情15', '2026-02-15 08:00:00'),
('技术前沿16', '内容详情16', '2026-02-16 08:00:00'),
('技术前沿17', '内容详情17', '2026-02-17 08:00:00'),
('技术前沿18', '内容详情18', '2026-02-18 08:00:00'),
('技术前沿19', '内容详情19', '2026-02-19 08:00:00'),
('技术前沿20', '内容详情20', '2026-02-20 08:00:00'),
('技术前沿21', '内容详情21', '2026-02-21 08:00:00'),
('技术前沿22', '内容详情22', '2026-02-22 08:00:00'),
('技术前沿23', '内容详情23', '2026-02-23 08:00:00'),
('技术前沿24', '内容详情24', '2026-02-24 08:00:00'),
('技术前沿25', '内容详情25', '2026-02-25 08:00:00'),
('技术前沿26', '内容详情26', '2026-02-26 08:00:00'),
('技术前沿27', '内容详情27', '2026-02-27 08:00:00'),
('技术前沿28', '内容详情28', '2026-02-28 08:00:00'),
('技术前沿29', '内容详情29', '2026-03-01 08:00:00'),
('技术前沿30', '内容详情30', '2026-03-02 08:00:00');

-- Announcements
INSERT INTO `announcement` (`title`, `content`, `content_type`, `target_type`, `is_pinned`, `pin_order`, `status`, `effective_time`, `expire_time`, `created_by`, `created_by_name`, `published_at`, `created_at`) VALUES
('关于2026年春季学期开学的重要通知', '各位同学请注意，2026年春季学期将于3月1日正式开学，请提前做好返校准备，按时到校注册。如有特殊情况不能按时返校，请提前向辅导员请假。', 'TEXT', 'ALL', 1, 1, 'PUBLISHED', '2026-02-20 00:00:00', '2026-12-31 23:59:59', 1, '张老师', '2026-02-20 09:00:00', '2026-02-20 08:00:00'),
('图书馆延长开放时间的通知', '为满足同学们的学习需求，图书馆从即日起延长开放时间：周一至周五 7:00-22:30，周六周日 8:00-22:00。请同学们合理安排学习时间。', 'TEXT', 'ALL', 1, 2, 'PUBLISHED', '2026-02-22 00:00:00', '2026-06-30 23:59:59', 2, '李老师', '2026-02-22 10:00:00', '2026-02-22 09:00:00'),
('计算机2101班课程调整通知', '本周五的Java程序设计课程因故调整至下周一上午第3-4节，地点不变。请同学们相互转告，按时上课。', 'TEXT', 'SPECIFIED', 0, 0, 'PUBLISHED', '2026-02-25 00:00:00', '2026-03-10 23:59:59', 1, '张老师', '2026-02-25 14:00:00', '2026-02-25 13:00:00'),
('软件2101班实验课安排', '软件工程实验班的同学们注意，本周六下午将进行第一次实验课，请提前完成预习报告，准时到达实验楼302教室。', 'TEXT', 'SPECIFIED', 0, 0, 'PUBLISHED', '2026-02-26 00:00:00', '2026-03-05 23:59:59', 5, '孙老师', '2026-02-26 11:00:00', '2026-02-26 10:00:00'),
('全校网络系统维护通知', '本周日凌晨2:00-6:00将进行全校网络系统升级维护，届时校园网将暂停服务，请同学们提前下载好所需资料。', 'TEXT', 'ALL', 0, 0, 'PUBLISHED', '2026-02-28 00:00:00', '2026-03-01 23:59:59', 6, '周老师', '2026-02-27 16:00:00', '2026-02-27 15:00:00'),
('期中考试安排预告', '本学期期中考试将于第8周进行，请同学们提前做好复习准备。具体考试时间表将另行通知。', 'TEXT', 'ALL', 0, 0, 'DRAFT', NULL, NULL, 2, '李老师', NULL, '2026-02-28 10:00:00'),
('AI2101班项目答辩通知', '人工智能实验班的项目答辩定于下周三下午进行，请各组同学准备好演示PPT和项目文档。', 'TEXT', 'SPECIFIED', 0, 0, 'PUBLISHED', '2026-03-01 00:00:00', '2026-03-15 23:59:59', 4, '赵老师', '2026-02-28 15:00:00', '2026-02-28 14:00:00'),
('校园招聘会预告', '3月15日将在体育馆举办春季校园招聘会，届时将有50余家企业到场，欢迎同学们积极参加。', 'TEXT', 'ALL', 0, 0, 'PUBLISHED', '2026-03-01 00:00:00', '2026-03-20 23:59:59', 3, '王老师', '2026-03-01 09:00:00', '2026-03-01 08:00:00'),
('计算机2102班班会通知', '计算机2102班将于本周四下午召开主题班会，请全体同学准时参加。', 'TEXT', 'SPECIFIED', 0, 0, 'PUBLISHED', '2026-03-02 00:00:00', '2026-03-06 23:59:59', 1, '张老师', '2026-03-02 10:00:00', '2026-03-02 09:00:00'),
('暑期实习报名开始', '2026年暑期实习报名工作已开始，请有意向的同学在教务系统中报名，截止日期为4月30日。', 'TEXT', 'ALL', 0, 0, 'PUBLISHED', '2026-03-05 00:00:00', '2026-04-30 23:59:59', 3, '王老师', '2026-03-05 11:00:00', '2026-03-05 10:00:00');

-- Announcement-Class Associations
INSERT INTO `announcement_class` (`announcement_id`, `class_id`, `class_name`) VALUES
(3, 1, '计算机2101'),
(4, 3, '软件2101'),
(7, 7, 'AI2101'),
(9, 2, '计算机2102');

-- (17) 作业表 homework
CREATE TABLE `homework` (
    `homework_id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL COMMENT '作业标题',
    `description` TEXT COMMENT '要求说明',
    `class_id` INT NOT NULL COMMENT '所属班级ID',
    `class_name` VARCHAR(100) COMMENT '班级名称(冗余)',
    `deadline` DATETIME NOT NULL COMMENT '截止时间',
    `full_score` DECIMAL(6,2) NOT NULL DEFAULT 100.00 COMMENT '满分分值',
    `created_by` INT COMMENT '创建教师ID',
    `created_by_name` VARCHAR(50) COMMENT '创建教师姓名',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态：PUBLISHED已发布/CLOSED已截止',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_class_id` (`class_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';

-- (18) 作业提交记录表 homework_submission
CREATE TABLE `homework_submission` (
    `submission_id` INT AUTO_INCREMENT PRIMARY KEY,
    `homework_id` INT NOT NULL COMMENT '作业ID',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `student_name` VARCHAR(50) COMMENT '学生姓名(冗余)',
    `student_no` VARCHAR(50) COMMENT '学号(冗余)',
    `file_url` VARCHAR(255) COMMENT '作业文件路径',
    `file_name` VARCHAR(255) COMMENT '原始文件名',
    `submission_count` INT NOT NULL DEFAULT 0 COMMENT '提交次数',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NOT_SUBMITTED' COMMENT '状态：NOT_SUBMITTED未提交/SUBMITTED已提交/GRADED已批改/REJECTED被打回/OVERDUE逾期',
    `score` DECIMAL(6,2) COMMENT '得分',
    `comment` TEXT COMMENT '教师评语',
    `submitted_at` DATETIME COMMENT '最后提交时间',
    `graded_at` DATETIME COMMENT '批改时间',
    `graded_by` INT COMMENT '批改教师ID',
    `graded_by_name` VARCHAR(50) COMMENT '批改教师姓名',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_homework_student` (`homework_id`, `student_id`),
    INDEX `idx_homework_id` (`homework_id`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交记录表';

-- Homework sample data
INSERT INTO `homework` (`title`, `description`, `class_id`, `class_name`, `deadline`, `full_score`, `created_by`, `created_by_name`, `status`, `created_at`) VALUES
('Java第1章作业', '完成教材第1章课后习题，提交Word文档。', 1, '计算机2101', '2026-06-30 23:59:59', 100.00, 1, '张老师', 'PUBLISHED', '2026-06-10 10:00:00'),
('数据库设计作业', '设计一个学生管理系统的ER图，提交PDF文档。', 1, '计算机2101', '2026-07-05 23:59:59', 100.00, 2, '李老师', 'PUBLISHED', '2026-06-11 14:00:00'),
('Python数据分析报告', '完成期末数据分析报告，不少于3000字。', 3, '软件2101', '2026-06-25 23:59:59', 100.00, 5, '孙老师', 'PUBLISHED', '2026-06-08 09:00:00'),
('算法实验报告', '实现快速排序算法并分析时间复杂度，提交源代码和报告。', 2, '计算机2102', '2026-06-28 23:59:59', 100.00, 3, '王老师', 'PUBLISHED', '2026-06-09 11:00:00'),
('Web前端大作业', '使用HTML/CSS/JS实现一个个人博客网站，提交源代码压缩包。', 7, 'AI2101', '2026-07-10 23:59:59', 100.00, 28, '华老师', 'PUBLISHED', '2026-06-12 08:00:00');

-- Homework submission sample data
INSERT INTO `homework_submission` (`homework_id`, `student_id`, `student_name`, `student_no`, `file_url`, `file_name`, `submission_count`, `status`, `score`, `comment`, `submitted_at`, `graded_at`, `graded_by`, `graded_by_name`) VALUES
(1, 1, '小明', 'S2021001', '/uploads/homework1_stu1.docx', 'Java第1章作业-小明.docx', 1, 'SUBMITTED', NULL, NULL, '2026-06-12 15:30:00', NULL, NULL, NULL),
(1, 2, '小红', 'S2021002', '/uploads/homework1_stu2.docx', 'Java第1章作业-小红.docx', 2, 'GRADED', 92.50, '完成很好，代码规范，继续保持！', '2026-06-11 10:20:00', '2026-06-11 16:00:00', 1, '张老师'),
(1, 3, '小王', 'S2021003', NULL, NULL, 0, 'NOT_SUBMITTED', NULL, NULL, NULL, NULL, NULL, NULL),
(3, 5, '小赵', 'S2021005', '/uploads/homework3_stu5.pdf', '数据分析报告-小赵.pdf', 1, 'REJECTED', NULL, '报告结构不完整，请补充数据可视化部分，在截止前重新提交。', '2026-06-10 14:00:00', '2026-06-10 18:00:00', 5, '孙老师');

-- (19) 可选课程表 elective_course
CREATE TABLE `elective_course` (
    `course_id` INT AUTO_INCREMENT PRIMARY KEY,
    `course_name` VARCHAR(255) NOT NULL COMMENT '课程名称',
    `description` TEXT COMMENT '课程简介',
    `teacher_id` INT NOT NULL COMMENT '授课教师ID',
    `teacher_name` VARCHAR(50) COMMENT '授课教师姓名',
    `capacity` INT NOT NULL DEFAULT 30 COMMENT '容量上限',
    `enrolled_count` INT NOT NULL DEFAULT 0 COMMENT '已选人数',
    `enroll_start_time` DATETIME NOT NULL COMMENT '选课开始时间',
    `enroll_end_time` DATETIME NOT NULL COMMENT '选课结束时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN开放选课/CLOSED已截止',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_teacher_id` (`teacher_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_enroll_time` (`enroll_start_time`, `enroll_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可选课程表';

-- (20) 选课记录表 course_enrollment
CREATE TABLE `course_enrollment` (
    `enrollment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `course_id` INT NOT NULL COMMENT '课程ID',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `student_name` VARCHAR(50) COMMENT '学生姓名',
    `student_no` VARCHAR(50) COMMENT '学号',
    `class_name` VARCHAR(100) COMMENT '班级名称',
    `enroll_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENROLLED' COMMENT '状态：ENROLLED已选/WITHDRAWN已退选',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_course_student` (`course_id`, `student_id`),
    INDEX `idx_course_id` (`course_id`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录表';

-- Elective courses sample data
INSERT INTO `elective_course` (`course_name`, `description`, `teacher_id`, `teacher_name`, `capacity`, `enrolled_count`, `enroll_start_time`, `enroll_end_time`, `status`) VALUES
('人工智能导论', '介绍人工智能的基本概念、发展历程、主要技术方向和应用场景，包括机器学习、深度学习、自然语言处理等前沿领域。', 4, '赵老师', 50, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('Python数据分析', '学习使用Python进行数据处理与分析，掌握NumPy、Pandas、Matplotlib等库的使用，培养数据思维与分析能力。', 5, '孙老师', 40, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('区块链技术与应用', '深入了解区块链技术原理，包括共识机制、智能合约、分布式账本等，并探讨其在各行业的实际应用。', 3, '王老师', 30, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('Web前端开发实战', '系统学习HTML5、CSS3、JavaScript及主流前端框架，掌握响应式设计和现代前端开发流程。', 28, '华老师', 45, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('算法设计与竞赛', '深入学习常用算法与数据结构，训练编程竞赛思维，提升代码能力与问题解决能力。', 2, '李老师', 35, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('网络安全基础', '介绍网络安全基本概念、常见攻击与防御技术、加密算法、防火墙配置等内容，培养安全意识。', 6, '周老师', 40, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('移动应用开发', '学习Android/iOS移动应用开发，掌握移动应用架构设计、UI开发、数据存储等核心技术。', 7, '吴老师', 30, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('云计算与容器技术', '学习云计算基础、Docker容器、Kubernetes编排、微服务架构等云原生技术。', 26, '曹老师', 35, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('软件测试与质量保证', '系统学习软件测试理论与方法，包括黑盒测试、白盒测试、自动化测试工具及质量管理流程。', 20, '许老师', 40, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('UI/UX设计基础', '学习用户界面与用户体验设计的基本原理、设计规范、原型工具使用，培养设计思维。', 29, '金老师', 25, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('机器学习实战', '深入学习监督学习、非监督学习等经典机器学习算法，并通过项目实践提升应用能力。', 15, '韩老师', 30, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN'),
('大数据技术栈', '学习Hadoop、Spark、Flink等大数据处理框架，掌握海量数据存储与分析技术。', 27, '严老师', 35, 0, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 'OPEN');

-- (21) 签到任务表 sign_in_task
CREATE TABLE `sign_in_task` (
    `task_id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL COMMENT '签到主题',
    `class_id` INT NOT NULL COMMENT '班级ID',
    `class_name` VARCHAR(100) COMMENT '班级名称(冗余)',
    `duration_minutes` INT NOT NULL DEFAULT 5 COMMENT '有效时长(分钟)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ONGOING' COMMENT '状态：ONGOING进行中/ENDED已结束',
    `created_by` INT COMMENT '创建教师ID',
    `created_by_name` VARCHAR(50) COMMENT '创建教师姓名',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间(=开始+时长)',
    `signed_count` INT NOT NULL DEFAULT 0 COMMENT '已签到人数',
    `absent_count` INT NOT NULL DEFAULT 0 COMMENT '缺勤人数',
    `leave_count` INT NOT NULL DEFAULT 0 COMMENT '请假人数',
    `total_students` INT NOT NULL DEFAULT 0 COMMENT '班级总人数',
    `attendance_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '出勤率(%)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_class_id` (`class_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_by` (`created_by`),
    INDEX `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到任务表';

-- (22) 签到记录表 sign_in_record
CREATE TABLE `sign_in_record` (
    `record_id` INT AUTO_INCREMENT PRIMARY KEY,
    `task_id` INT NOT NULL COMMENT '签到任务ID',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `student_name` VARCHAR(50) COMMENT '学生姓名(冗余)',
    `student_no` VARCHAR(50) COMMENT '学号(冗余)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ABSENT' COMMENT '状态：SIGNED已签到/ABSENT缺勤/LEAVE请假',
    `sign_in_time` DATETIME COMMENT '签到时间',
    `sign_in_ip` VARCHAR(50) COMMENT '签到IP地址',
    `is_manual` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否教师手工补登：0否/1是',
    `manual_by` INT COMMENT '手工补登教师ID',
    `manual_by_name` VARCHAR(50) COMMENT '手工补登教师姓名',
    `manual_time` DATETIME COMMENT '手工补登时间',
    `remark` VARCHAR(255) COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_task_student` (`task_id`, `student_id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- (23) 私信会话表 conversation
CREATE TABLE `conversation` (
    `conversation_id` INT AUTO_INCREMENT PRIMARY KEY,
    `student_id` INT NOT NULL COMMENT '学生ID(user表uid)',
    `student_name` VARCHAR(50) COMMENT '学生姓名(冗余)',
    `teacher_id` INT NOT NULL COMMENT '教师ID(teacher表tid)',
    `teacher_name` VARCHAR(50) COMMENT '教师姓名(冗余)',
    `last_message` TEXT COMMENT '最后一条消息预览(冗余)',
    `last_message_time` DATETIME COMMENT '最后消息时间(用于排序)',
    `student_unread_count` INT NOT NULL DEFAULT 0 COMMENT '学生未读消息数',
    `teacher_unread_count` INT NOT NULL DEFAULT 0 COMMENT '教师未读消息数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_student_teacher` (`student_id`, `teacher_id`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_teacher_id` (`teacher_id`),
    INDEX `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信会话表';

-- (24) 私信消息表 message
CREATE TABLE `message` (
    `message_id` INT AUTO_INCREMENT PRIMARY KEY,
    `conversation_id` INT NOT NULL COMMENT '会话ID',
    `sender_type` VARCHAR(20) NOT NULL COMMENT '发送方类型：STUDENT/TEACHER',
    `sender_id` INT NOT NULL COMMENT '发送方ID',
    `sender_name` VARCHAR(50) COMMENT '发送方姓名(冗余)',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读/1已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_sender` (`sender_type`, `sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- Conversation sample data
INSERT INTO `conversation` (`student_id`, `student_name`, `teacher_id`, `teacher_name`, `last_message`, `last_message_time`, `student_unread_count`, `teacher_unread_count`) VALUES
(1, '小明', 1, '张老师', '老师，请问Java多线程如何实现？', '2026-06-10 09:30:00', 0, 1),
(2, '小红', 1, '张老师', '我的作业已经提交了，请查收。', '2026-06-10 14:20:00', 1, 0),
(1, '小明', 2, '李老师', '数据库索引优化的问题想请教您', '2026-06-11 10:15:00', 0, 2),
(3, '小王', 3, '王老师', '区块链项目有一些疑问', '2026-06-09 16:45:00', 0, 0),
(5, '小赵', 4, '赵老师', 'AI实验的代码报错了，能帮忙看看吗？', '2026-06-11 11:00:00', 1, 1);

-- Message sample data
INSERT INTO `message` (`conversation_id`, `sender_type`, `sender_id`, `sender_name`, `content`, `is_read`, `created_at`) VALUES
(1, 'STUDENT', 1, '小明', '张老师您好，我有关于Java的问题想请教。', 1, '2026-06-09 08:00:00'),
(1, 'TEACHER', 1, '张老师', '你好小明，请问是什么问题？', 1, '2026-06-09 08:30:00'),
(1, 'STUDENT', 1, '小明', '老师，请问Java多线程如何实现？', 0, '2026-06-10 09:30:00'),
(2, 'STUDENT', 2, '小红', '张老师，我的作业已经提交了，请查收。', 0, '2026-06-10 14:20:00'),
(3, 'STUDENT', 1, '小明', '李老师，我想问关于数据库索引的问题。', 1, '2026-06-10 09:00:00'),
(3, 'TEACHER', 2, '李老师', '好的，你说说看。', 1, '2026-06-10 09:15:00'),
(3, 'STUDENT', 1, '小明', '数据库索引优化的问题想请教您', 0, '2026-06-11 10:15:00'),
(3, 'STUDENT', 1, '小明', '比如复合索引的最左前缀原则不太理解', 0, '2026-06-11 10:16:00'),
(4, 'STUDENT', 3, '小王', '王老师，区块链项目有一些疑问', 1, '2026-06-09 16:00:00'),
(4, 'TEACHER', 3, '王老师', '你具体哪里不清楚？', 1, '2026-06-09 16:30:00'),
(4, 'STUDENT', 3, '小王', '智能合约的部署流程不太明白', 1, '2026-06-09 16:45:00'),
(5, 'STUDENT', 5, '小赵', '赵老师，AI实验的代码报错了，能帮忙看看吗？', 0, '2026-06-11 11:00:00');
