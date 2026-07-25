import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import relativeTime from 'dayjs/plugin/relativeTime';

// Extend dayjs with plugins
dayjs.extend(relativeTime);

// Set the Chinese locale
dayjs.locale('zh-cn');

// Export the configured dayjs instance
export default dayjs;

export { dayjs as Dayjs };
