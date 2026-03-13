import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  Chart as ChartJS,
  ArcElement,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { Doughnut, Bar } from 'react-chartjs-2';
import './App.css';

ChartJS.register(ArcElement, CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const COLORS = ['#00d9ff', '#00ff88', '#a855f7', '#ff9500', '#ff3b8e', '#ffd000', '#3b82f6', '#14b8a6'];

function App() {
  const [costData, setCostData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);

  const months = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'];

  const fetchCostData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get(`/api/cost/monthly`, {
        params: { year: selectedYear, month: selectedMonth },
      });
      setCostData(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Connection failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCostData(); }, [selectedYear, selectedMonth]);

  const getDoughnutData = () => {
    if (!costData?.serviceBreakdown?.length) return null;
    return {
      labels: costData.serviceBreakdown.map(s => s.serviceName.replace('Amazon ', '').replace('AWS ', '')),
      datasets: [{
        data: costData.serviceBreakdown.map(s => parseFloat(s.totalCost)),
        backgroundColor: COLORS,
        borderColor: '#1a1a1a',
        borderWidth: 3,
        hoverOffset: 8,
      }],
    };
  };

  const getBarData = () => {
    if (!costData?.serviceBreakdown?.length) return null;
    return {
      labels: costData.serviceBreakdown.map(s => s.serviceName.replace('Amazon ', '').replace('AWS ', '')),
      datasets: [{
        data: costData.serviceBreakdown.map(s => parseFloat(s.totalCost)),
        backgroundColor: COLORS.map(c => c + '99'),
        borderColor: COLORS,
        borderWidth: 2,
        borderRadius: 6,
      }],
    };
  };

  const doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '60%',
    plugins: {
      legend: {
        display: true,
        position: 'right',
        labels: {
          color: '#ffffff',
          padding: 14,
          usePointStyle: true,
          pointStyle: 'circle',
          font: { family: 'JetBrains Mono', size: 11, weight: '500' },
          generateLabels: (chart) => {
            const data = chart.data;
            if (data.labels.length && data.datasets.length) {
              return data.labels.map((label, i) => {
                const value = data.datasets[0].data[i];
                return {
                  text: `${label} ($${value.toFixed(2)})`,
                  fillStyle: COLORS[i],
                  strokeStyle: COLORS[i],
                  fontColor: '#ffffff',
                  lineWidth: 0,
                  pointStyle: 'circle',
                  hidden: false,
                  index: i,
                };
              });
            }
            return [];
          },
        },
      },
      tooltip: {
        backgroundColor: '#1a1a1a',
        titleColor: '#fff',
        bodyColor: '#ccc',
        borderColor: '#333',
        borderWidth: 1,
        padding: 12,
        cornerRadius: 8,
        titleFont: { family: 'JetBrains Mono', size: 12 },
        bodyFont: { family: 'JetBrains Mono', size: 11 },
        callbacks: {
          label: ctx => {
            const total = ctx.dataset.data.reduce((a, b) => a + b, 0);
            const pct = ((ctx.raw / total) * 100).toFixed(1);
            return ` $${ctx.raw.toFixed(2)} (${pct}%)`;
          },
        },
      },
    },
  };

  const barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1a1a1a',
        titleColor: '#fff',
        bodyColor: '#ccc',
        borderColor: '#333',
        borderWidth: 1,
        padding: 12,
        cornerRadius: 8,
        callbacks: { label: ctx => ` $${ctx.raw.toFixed(2)}` },
      },
    },
    scales: {
      x: { grid: { color: '#333' }, ticks: { color: '#aaa', font: { size: 11 }, callback: v => `$${v}` } },
      y: { grid: { display: false }, ticks: { color: '#fff', font: { size: 12, family: 'JetBrains Mono' } } },
    },
  };

  return (
    <div className="console-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <div className="logo-box">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
              </svg>
            </div>
            <div className="logo-text">
              <h1>CloudCost</h1>
              <span>v1.0.0</span>
            </div>
          </div>
        </div>

        <div className="sidebar-stats">
          <div className="stat-block cyan">
            <div className="stat-label">Total Cost</div>
            <div className="stat-value">${costData ? parseFloat(costData.totalCost).toFixed(2) : '0.00'}</div>
            <div className="stat-sub">{costData?.currency || 'USD'}</div>
          </div>

          <div className="stat-block green">
            <div className="stat-label">Services</div>
            <div className="stat-value">{costData?.numberOfServices || 0}</div>
            <div className="stat-sub">active services</div>
          </div>

          <div className="stat-block orange">
            <div className="stat-label">Period</div>
            <div className="stat-value">{months[selectedMonth - 1]?.substring(0, 3)}</div>
            <div className="stat-sub">{selectedYear}</div>
          </div>

          <div className="status-indicator">
            <span className="status-dot"></span>
            <span className="status-text">AWS Connected</span>
          </div>
        </div>

        <div className="sidebar-footer">
          <p>Powered by AWS Cost Explorer</p>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <div className="top-bar">
          <div className="breadcrumb">
            <span>aws</span> / <span>cost-explorer</span> / <span>{months[selectedMonth - 1]} {selectedYear}</span>
          </div>
          <div className="top-actions">
            <div className="top-controls">
              <select className="top-select" value={selectedYear} onChange={e => setSelectedYear(parseInt(e.target.value))}>
                {[2024, 2025, 2026].map(y => <option key={y} value={y}>{y}</option>)}
              </select>
              <select className="top-select" value={selectedMonth} onChange={e => setSelectedMonth(parseInt(e.target.value))}>
                {months.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
              </select>
              <button onClick={fetchCostData} className="top-refresh-btn" title="Refresh Data">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M23 4v6h-6M1 20v-6h6"/>
                  <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        {loading ? (
          <div className="loading-panel">
            <div className="loader-ring"></div>
            <p>Fetching data...</p>
          </div>
        ) : error ? (
          <div className="error-panel">
            <p>{error}</p>
            <button onClick={fetchCostData}>Retry</button>
          </div>
        ) : !costData?.serviceBreakdown?.length ? (
          <div className="no-data-panel">
            <div className="no-data-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
            <h3>No Data Available</h3>
            <p>No cost data for {months[selectedMonth - 1]} {selectedYear}</p>
            <code>POST /api/cost/fetch?start={selectedYear}-{String(selectedMonth).padStart(2, '0')}-01</code>
          </div>
        ) : (
          <div className="content-grid">
            {/* Doughnut Chart with Legend */}
            <div className="grid-panel">
              <div className="panel-header">
                <div className="panel-title">
                  <div className="panel-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21.21 15.89A10 10 0 1 1 8 2.83"/><path d="M22 12A10 10 0 0 0 12 2v10z"/>
                    </svg>
                  </div>
                  <div>
                    <h3>Distribution</h3>
                    <p>cost breakdown</p>
                  </div>
                </div>
                <span className="panel-badge">PIE</span>
              </div>
              <div className="panel-content">
                <div className="chart-wrapper">
                  <Doughnut data={getDoughnutData()} options={doughnutOptions} />
                </div>
              </div>
            </div>

            {/* Bar Chart */}
            <div className="grid-panel">
              <div className="panel-header">
                <div className="panel-title">
                  <div className="panel-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
                    </svg>
                  </div>
                  <div>
                    <h3>Comparison</h3>
                    <p>service costs</p>
                  </div>
                </div>
                <span className="panel-badge">BAR</span>
              </div>
              <div className="panel-content">
                <div className="chart-wrapper">
                  <Bar data={getBarData()} options={barOptions} />
                </div>
              </div>
            </div>

            {/* Service List */}
            <div className="grid-panel full-width">
              <div className="panel-header">
                <div className="panel-title">
                  <div className="panel-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
                      <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
                    </svg>
                  </div>
                  <div>
                    <h3>Services</h3>
                    <p>{costData.numberOfServices} active</p>
                  </div>
                </div>
                <span className="panel-badge">LIST</span>
              </div>
              <div className="service-grid">
                {costData.serviceBreakdown.map((service, i) => (
                  <div key={i} className="service-item">
                    <div className="service-icon" style={{ background: COLORS[i % COLORS.length] }}>
                      {service.serviceName.split(' ').map(w => w[0]).join('').substring(0, 2)}
                    </div>
                    <div className="service-info">
                      <div className="service-name">{service.serviceName.replace('Amazon ', '').replace('AWS ', '')}</div>
                      <div className="service-full">{service.serviceName}</div>
                    </div>
                    <div className="service-cost">
                      <div className="cost-amount">${parseFloat(service.totalCost).toFixed(2)}</div>
                      <div className="cost-percent">{parseFloat(service.percentageOfTotal).toFixed(1)}%</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
        <footer className="page-footer">
          Created by <a href="https://www.linkedin.com/in/lalit-rajpurohit" target="_blank" rel="noopener noreferrer">Lalit Rajpurohit</a>
        </footer>
      </main>
    </div>
  );
}

export default App;
