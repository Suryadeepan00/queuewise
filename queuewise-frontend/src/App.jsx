import { useEffect, useState } from 'react'

function App() {
  const [serviceType, setServiceType] = useState('')
  const [token, setToken] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const [calledResult, setCalledResult] = useState(null)
  const [callError, setCallError] = useState('')
  const [calling, setCalling] = useState(false)

  const [completeError, setCompleteError] = useState('')
  const [completing, setCompleting] = useState(false)
  const [skipError, setSkipError] = useState('')
  const [skipping, setSkipping] = useState(false)

  const [waitingTokens, setWaitingTokens] = useState([])
  const [queueError, setQueueError] = useState('')
  const [view, setView] = useState('visitor')

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setToken(null)

    if (!serviceType.trim()) {
      setError('Please enter a service type.')
      return
    }

    setLoading(true)

    try {
      const response = await fetch('/api/tokens', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ serviceType: serviceType.trim() }),
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.error || 'Could not create token.')
      }

      setToken(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleCallNext() {
    setCallError('')
    setCalledResult(null)
    setCalling(true)

    try {
      const response = await fetch('/api/tokens/call-next', {
        method: 'POST',
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.error || 'Could not call the next token.')
      }

      setCalledResult(data)
    } catch (err) {
      setCallError(err.message)
    } finally {
      setCalling(false)
    }
  }

  async function handleComplete() {
    if (calledResult?.status !== 'CALLED') return

    setCompleteError('')
    setCompleting(true)

    try {
      const response = await fetch(
        `/api/tokens/${encodeURIComponent(calledResult.tokenNumber)}/complete`,
        { method: 'POST' }
      )

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.error || 'Could not complete the token.')
      }

      setCalledResult(data)
    } catch (err) {
      setCompleteError(err.message)
    } finally {
      setCompleting(false)
    }
  }

  async function handleSkip() {
    if (calledResult?.status !== 'CALLED') return

    setSkipError('')
    setSkipping(true)

    try {
      const response = await fetch(
        `/api/tokens/${encodeURIComponent(calledResult.tokenNumber)}/skip`,
        { method: 'POST' }
      )

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.error || 'Could not skip the token.')
      }

      setCalledResult(data)
    } catch (err) {
      setSkipError(err.message)
    } finally {
      setSkipping(false)
    }
  }

  useEffect(() => {
    let ignore = false

    async function loadWaitingTokens() {
      try {
        const response = await fetch('/api/tokens/waiting')

        if (!response.ok) {
          throw new Error(`Could not load the queue (${response.status}).`)
        }

        const data = await response.json()

        if (!ignore) {
          setWaitingTokens(data)
          setQueueError('')
        }
      } catch (err) {
        if (!ignore) setQueueError(err.message)
      }
    }

    loadWaitingTokens()

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    const events = new EventSource('/api/tokens/events')

    async function refreshQueue() {
      try {
        const response = await fetch('/api/tokens/waiting')

        if (!response.ok) {
          throw new Error(`Could not refresh the queue (${response.status}).`)
        }

        const data = await response.json()
        setWaitingTokens(data)
        setQueueError('')
      } catch (err) {
        setQueueError(err.message)
      }
    }

    events.addEventListener('token-updated', refreshQueue)

    return () => {
      events.removeEventListener('token-updated', refreshQueue)
      events.close()
    }
  }, [])

  return (
    <main>
      <h1>QueueWise</h1>

      <nav aria-label="QueueWise views">
        <nav aria-label="QueueWise views">
          <button
            type="button"
            className={view === 'visitor' ? 'nav-active' : ''}
            onClick={() => setView('visitor')}
          >
            Visitor
          </button>

          <button
            type="button"
            className={view === 'staff' ? 'nav-active' : ''}
            onClick={() => setView('staff')}
          >
            Staff
          </button>

          <button
            type="button"
            className={view === 'queue' ? 'nav-active' : ''}
            onClick={() => setView('queue')}
          >
            Queue display
          </button>
        </nav>
      </nav>

      {view === 'visitor' && (
        <section>
          <h2>Get your token</h2>

          <form onSubmit={handleSubmit}>
            <label htmlFor="serviceType">Service type: </label>
            <input
              id="serviceType"
              value={serviceType}
              onChange={(event) => setServiceType(event.target.value)}
              placeholder="e.g. GENERAL"
            />
            <button type="submit" disabled={loading}>
              {loading ? 'Creating...' : 'Get token'}
            </button>
          </form>

          {token && (
            <div className="token-card" role="status">
              <p className="token-card-label">Your token number</p>
              <p className="token-card-number">{token.tokenNumber}</p>
              <p>Keep this number handy while you wait.</p>
            </div>
          )}
          {error && <p role="alert">{error}</p>}
        </section>
      )}

      {view === 'staff' && (
        <section>
          <h2>Staff: call next</h2>

          <button type="button" onClick={handleCallNext} disabled={calling}>
            {calling ? 'Calling...' : 'Call next'}
          </button>

          {calledResult && (
            <div className="token-card">
              <p className="token-card-label">
                {calledResult.status === 'CALLED'
                  ? 'Now serving'
                  : 'Last updated token'}
              </p>

              <p className="token-card-number">{calledResult.tokenNumber}</p>

              <div className="token-card-details">
                <span>{calledResult.serviceType}</span>
                <span className={`status-badge status-${calledResult.status.toLowerCase()}`}>
                  {calledResult.status}
                </span>
              </div>
            </div>
          )}
          {callError && <p role="alert">{callError}</p>}

          {calledResult?.status === 'CALLED' && (
            <>
              <button
                type="button"
                onClick={handleComplete}
                disabled={completing || skipping}
              >
                {completing ? 'Completing...' : 'Complete token'}
              </button>
              <button
                type="button"
                onClick={handleSkip}
                disabled={skipping || completing}
              >
                {skipping ? 'Skipping...' : 'Skip token'}
              </button>
            </>
          )}

          {completeError && <p role="alert">{completeError}</p>}
          {skipError && <p role="alert">{skipError}</p>}
        </section>
      )}

      {view === 'queue' && (
        <section>
          <h2>Waiting queue</h2>
          <p className="queue-count">
            {waitingTokens.length} {waitingTokens.length === 1 ? 'person' : 'people'} waiting
          </p>

          {queueError && <p role="alert">{queueError}</p>}

          {waitingTokens.length === 0 && !queueError ? (
            <p>No tokens waiting.</p>
          ) : (
            <ul className="queue-list">
              {waitingTokens.map((waitingToken) => (
                <li className="queue-item" key={waitingToken.tokenNumber}>
                  <span className="queue-number">{waitingToken.tokenNumber}</span>
                  <span>{waitingToken.serviceType}</span>
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
    </main>
  )
}

export default App