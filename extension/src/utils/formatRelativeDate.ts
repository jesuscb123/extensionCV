const relativeTimeFormatter = new Intl.RelativeTimeFormat('es', { numeric: 'auto' })

/** Formatea una fecha ISO-8601 como "hace 3 días" / "hace 2 horas", etc. */
export function formatRelativeDate(isoDate: string): string {
  const diffMs = new Date(isoDate).getTime() - Date.now()
  const diffMinutes = Math.round(diffMs / 60_000)

  if (Math.abs(diffMinutes) < 60) return relativeTimeFormatter.format(diffMinutes, 'minute')

  const diffHours = Math.round(diffMinutes / 60)
  if (Math.abs(diffHours) < 24) return relativeTimeFormatter.format(diffHours, 'hour')

  const diffDays = Math.round(diffHours / 24)
  return relativeTimeFormatter.format(diffDays, 'day')
}
