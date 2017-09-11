SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `prp`
--

-- --------------------------------------------------------

--
-- 表的结构 `prp_device`
--

CREATE TABLE `prp_device` (
  `id` int(11) NOT NULL,
  `device` varchar(40) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `prp_image`
--

CREATE TABLE `prp_image` (
  `id` int(11) NOT NULL,
  `title` varchar(50) NOT NULL,
  `person` int(11) NOT NULL,
  `data` varchar(3000) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `prp_person`
--

CREATE TABLE `prp_person` (
  `id` int(11) NOT NULL,
  `device` int(11) NOT NULL,
  `person` varchar(40) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `prp_device`
--
ALTER TABLE `prp_device`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `device` (`device`);

--
-- Indexes for table `prp_image`
--
ALTER TABLE `prp_image`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `IMAGE` (`title`,`person`),
  ADD KEY `FK_PERSON_IMAGE` (`person`);

--
-- Indexes for table `prp_person`
--
ALTER TABLE `prp_person`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `person` (`device`,`person`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `prp_device`
--
ALTER TABLE `prp_device`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- 使用表AUTO_INCREMENT `prp_image`
--
ALTER TABLE `prp_image`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- 使用表AUTO_INCREMENT `prp_person`
--
ALTER TABLE `prp_person`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- 限制导出的表
--

--
-- 限制表 `prp_image`
--
ALTER TABLE `prp_image`
  ADD CONSTRAINT `FK_PERSON_IMAGE` FOREIGN KEY (`person`) REFERENCES `prp_person` (`id`) ON DELETE CASCADE;

--
-- 限制表 `prp_person`
--
ALTER TABLE `prp_person`
  ADD CONSTRAINT `FK_ID` FOREIGN KEY (`device`) REFERENCES `prp_device` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
