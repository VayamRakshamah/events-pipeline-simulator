import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  Activity,
  AlertTriangle,
  Boxes,
  CheckCircle2,
  Flame,
  Gauge,
  GitBranch,
  Play,
  Radio,
  RefreshCw,
  Send,
  ShieldAlert,
  Trash2,
  Zap
} from "lucide-react";
import "./styles.css";

const params = new URLSearchParams(window.location.search);
const API_BASE = params.get("api") || import.meta.env.VITE_API_BASE || "http://localhost:8081/api";

const scenarios = [
  { value: "SUCCESS", label: "Success", icon: CheckCircle2 },
  { value: "VALIDATION_FAILURE", label: "Validation failure", icon: ShieldAlert },
  { value: "TRANSIENT_FAILURE", label: "Transient failure", icon: RefreshCw },
  { value: "POISON_MESSAGE", label: "Poison message", icon: AlertTriangle }
];

const statusTone = {
  RECEIVED: "neutral",
  PUBLISHED: "info",
  VALIDATING: "info",
  PROCESSING: "active",
  COMPLETED: "success",
  RETRYING: "warning",
  FAILED: "danger",
  DEAD_LETTERED: "danger"
};

function App() {
  const [events, setEvents] = useState([]);
  const [metrics, setMetrics] = useState(null);
  const [selectedId, setSelectedId] = useState(null);
  const [form, setForm] = useState({ sku: "SKU-4108", storeId: "BLR-1", quantityDelta: 12, scenario: "SUCCESS" });
  const [busy, setBusy] = useState(false);

  const selectedEvent = useMemo(() => events.find((event) => event.eventId === selectedId) || events[0], [events, selectedId]);

  async function refresh() {
    const [eventResponse, metricsResponse] = await Promise.all([
      fetch(`${API_BASE}/events`),
      fetch(`${API_BASE}/metrics/pipeline`)
    ]);
    if (eventResponse.ok) {
      const payload = await eventResponse.json();
      setEvents(payload);
      if (!selectedId && payload[0]) setSelectedId(payload[0].eventId);
    }
    if (metricsResponse.ok) {
      setMetrics(await metricsResponse.json());
    }
  }

  async function publishEvent() {
    setBusy(true);
    try {
      const response = await fetch(`${API_BASE}/events/inventory`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...form, quantityDelta: Number(form.quantityDelta) })
      });
      const payload = await response.json();
      setSelectedId(payload.eventId);
      await refresh();
    } finally {
      setBusy(false);
    }
  }

  async function publishBurst() {
    setBusy(true);
    try {
      await fetch(`${API_BASE}/scenarios/burst`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ count: 12 })
      });
      await refresh();
    } finally {
      setBusy(false);
    }
  }

  async function clearEvents() {
    setBusy(true);
    try {
      await fetch(`${API_BASE}/events`, { method: "DELETE" });
      setEvents([]);
      setMetrics(null);
      setSelectedId(null);
      await refresh();
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => {
    refresh().catch(() => {});
    const interval = window.setInterval(() => refresh().catch(() => {}), 1200);
    return () => window.clearInterval(interval);
  }, []);

  return (
    <main className="app-shell">
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark"><GitBranch size={18} /></span>
          <div>
            <h1>Event Pipeline Simulator</h1>
            <p>{API_BASE}</p>
          </div>
        </div>
        <div className="env-pill"><Radio size={15} /> Simulation ready</div>
      </header>

      <section className="metrics-strip">
        <Metric icon={Boxes} label="Total events" value={metrics?.totalEvents ?? 0} />
        <Metric icon={CheckCircle2} label="Success rate" value={`${Math.round((metrics?.successRate ?? 0) * 100)}%`} />
        <Metric icon={RefreshCw} label="Retried" value={metrics?.retriedEvents ?? 0} />
        <Metric icon={Flame} label="DLQ" value={metrics?.deadLetteredEvents ?? 0} />
        <Metric icon={Gauge} label="Avg latency" value={`${metrics?.averageLatencyMs ?? 0} ms`} />
      </section>

      <section className="workspace">
        <aside className="publisher panel">
          <div className="panel-title">
            <Send size={18} />
            <h2>Publish inventory event</h2>
          </div>
          <label>SKU</label>
          <input value={form.sku} onChange={(event) => setForm({ ...form, sku: event.target.value })} />
          <label>Store ID</label>
          <input value={form.storeId} onChange={(event) => setForm({ ...form, storeId: event.target.value })} />
          <label>Quantity delta</label>
          <input type="number" value={form.quantityDelta} onChange={(event) => setForm({ ...form, quantityDelta: event.target.value })} />
          <label>Scenario</label>
          <div className="scenario-grid">
            {scenarios.map((scenario) => {
              const Icon = scenario.icon;
              return (
                <button
                  key={scenario.value}
                  className={form.scenario === scenario.value ? "scenario selected" : "scenario"}
                  onClick={() => setForm({ ...form, scenario: scenario.value })}
                  type="button"
                >
                  <Icon size={16} />
                  {scenario.label}
                </button>
              );
            })}
          </div>
          <button className="primary-action" onClick={publishEvent} disabled={busy}>
            <Play size={16} /> Publish Event
          </button>
          <button className="secondary-action" onClick={publishBurst} disabled={busy}>
            <Zap size={16} /> Burst Traffic
          </button>
          <button className="ghost-action" onClick={clearEvents} disabled={busy}>
            <Trash2 size={16} /> Clear
          </button>
        </aside>

        <section className="center-stack">
          <PipelineMap />
          <EventTable events={events} selectedId={selectedEvent?.eventId} onSelect={setSelectedId} />
        </section>

        <EventDetail event={selectedEvent} />
      </section>
    </main>
  );
}

function Metric({ icon: Icon, label, value }) {
  return (
    <div className="metric">
      <Icon size={18} />
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function PipelineMap() {
  const nodes = ["API Gateway", "inventory.events", "Worker", "retry topic", "DLQ", "Notify"];
  return (
    <section className="pipeline-map panel">
      <div className="panel-title">
        <Activity size={18} />
        <h2>Pipeline topology</h2>
      </div>
      <div className="node-row">
        {nodes.map((node, index) => (
          <React.Fragment key={node}>
            <div className={index === 4 ? "node danger" : "node"}>{node}</div>
            {index < nodes.length - 1 && <div className="edge" />}
          </React.Fragment>
        ))}
      </div>
    </section>
  );
}

function EventTable({ events, selectedId, onSelect }) {
  return (
    <section className="event-table panel">
      <div className="panel-title">
        <Radio size={18} />
        <h2>Live event stream</h2>
      </div>
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Event</th>
              <th>SKU</th>
              <th>Scenario</th>
              <th>Status</th>
              <th>Retry</th>
              <th>Latency</th>
            </tr>
          </thead>
          <tbody>
            {events.map((event) => (
              <tr key={event.eventId} className={selectedId === event.eventId ? "selected-row" : ""} onClick={() => onSelect(event.eventId)}>
                <td>{event.eventId}</td>
                <td>{event.sku}</td>
                <td>{formatScenario(event.scenario)}</td>
                <td><StatusBadge status={event.status} /></td>
                <td>{event.retryCount}</td>
                <td>{event.latencyMs} ms</td>
              </tr>
            ))}
            {events.length === 0 && (
              <tr>
                <td colSpan="6" className="empty-row">No events yet. Publish one to start the pipeline.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function EventDetail({ event }) {
  return (
    <aside className="detail panel">
      <div className="panel-title">
        <GitBranch size={18} />
        <h2>Selected event</h2>
      </div>
      {!event && <p className="muted">No selected event.</p>}
      {event && (
        <>
          <div className="detail-head">
            <strong>{event.eventId}</strong>
            <StatusBadge status={event.status} />
          </div>
          <dl className="detail-grid">
            <div><dt>SKU</dt><dd>{event.sku}</dd></div>
            <div><dt>Store</dt><dd>{event.storeId}</dd></div>
            <div><dt>Delta</dt><dd>{event.quantityDelta}</dd></div>
            <div><dt>Retries</dt><dd>{event.retryCount}</dd></div>
          </dl>
          <h3>Topic path</h3>
          <div className="topic-path">{event.topicPath.length ? event.topicPath.join(" -> ") : "API accepted"}</div>
          {event.failureReason && <div className="failure">{event.failureReason}</div>}
          <h3>Timeline</h3>
          <ol className="timeline">
            {event.timeline.map((entry, index) => (
              <li key={`${entry.status}-${index}`}>
                <StatusBadge status={entry.status} />
                <p>{entry.message}</p>
                <small>{entry.topic || "api"} · {new Date(entry.timestamp).toLocaleTimeString()}</small>
              </li>
            ))}
          </ol>
        </>
      )}
    </aside>
  );
}

function StatusBadge({ status }) {
  return <span className={`status ${statusTone[status] || "neutral"}`}>{status.replaceAll("_", " ")}</span>;
}

function formatScenario(value) {
  return value.toLowerCase().split("_").map((word) => word[0].toUpperCase() + word.slice(1)).join(" ");
}

createRoot(document.getElementById("root")).render(<App />);
