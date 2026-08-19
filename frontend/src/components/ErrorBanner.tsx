export default function ErrorBanner({ message }: { message: string | null }) {
  if (message === null || message === '') {
    return null
  }
  return (
    <div className="error-box" role="alert">
      {message}
    </div>
  )
}
