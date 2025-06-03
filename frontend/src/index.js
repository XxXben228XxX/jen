// src/index.js
import React from 'react';
import ReactDOM from 'react-dom/client'; // Змінено: імпортуємо з 'react-dom/client'
import App from './App';
import './index.css'; // За бажанням

const root = ReactDOM.createRoot(document.getElementById('root')); // Змінено: використовуємо createRoot
root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);