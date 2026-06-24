import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export function formatDateTime(input: string | number | Date | undefined | null, pattern = 'YYYY-MM-DD HH:mm'): string {
  if (!input) return ''
  const d = dayjs(input)
  return d.isValid() ? d.format(pattern) : ''
}

export function formatRelative(input: string | number | Date | undefined | null): string {
  if (!input) return ''
  return dayjs(input).fromNow()
}

export { dayjs }