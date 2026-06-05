import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import relativeTime from 'dayjs/plugin/relativeTime';

// 扩展 dayjs 插件
dayjs.extend(relativeTime);

// 设置中文 locale
dayjs.locale('zh-cn');

// 导出配置好的 dayjs 实例
export default dayjs;
