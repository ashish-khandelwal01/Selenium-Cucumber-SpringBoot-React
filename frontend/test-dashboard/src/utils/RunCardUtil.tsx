export const formatDuration = (seconds: number): string => {
  const mins = Math.floor(seconds / 60);
  const secsRaw = seconds % 60;

  const secs =
    secsRaw % 1 === 0
      ? `${Math.floor(secsRaw)}`
      : secsRaw.toFixed(2);

  return `${mins > 0 ? mins + 'm ' : ''}${secs}s`;
};

export const formatDate = (date: string): string => {
  const options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  };
  return new Date(date).toLocaleString('en-US', options);
};