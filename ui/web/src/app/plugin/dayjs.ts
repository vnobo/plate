import dayjs from 'dayjs'; // 导入dayjs
import locale_zh from 'dayjs/locale/zh-cn'; // 导入本地化语言
import isLeapYear from 'dayjs/plugin/isLeapYear'; // 导入插件
import relativeTime from 'dayjs/plugin/relativeTime'; // 导入插件
import localeData from 'dayjs/plugin/localeData'; // 导入插件
import localizedFormat from 'dayjs/plugin/localizedFormat'; // 导入插件
import customParseFormat from 'dayjs/plugin/customParseFormat'; // 导入插件

dayjs.locale(locale_zh) // 使用本地化语言
dayjs.extend(relativeTime) // 使用插件
dayjs.extend(localeData) // 使用插件
dayjs.extend(localizedFormat) // 使用插件
dayjs.extend(isLeapYear) // 使用插件
dayjs.extend(customParseFormat) // 使用插件

export const Dayjs = dayjs; // 导出dayjs
