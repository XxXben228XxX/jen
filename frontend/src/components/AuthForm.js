// src/components/AuthForm.js
import React, { useState } from 'react';

function AuthForm({ onLoginSuccess }) {
    const [activeTab, setActiveTab] = useState('signin');
    const [message, setMessage] = useState({ type: '', text: '' });
    const apiUrl = 'http://localhost:8080';

    const showMessage = (type, text) => {
        setMessage({ type, text });
        setTimeout(() => setMessage({ type: '', text: '' }), 5000);
    };

    const handleSignupSubmit = async (event) => {
        event.preventDefault();
        setMessage({ type: '', text: '' });

        const username = event.target.signupUsername.value;
        const email = event.target.signupEmail.value;
        const password = event.target.signupPassword.value;
        const rolesInput = event.target.signupRoles.value;
        const roles = rolesInput ? rolesInput.split(',').map(role => role.trim()) : ['user'];

        try {
            const response = await fetch(`${apiUrl}/api/auth/signup`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, email, password, roles })
            });

            if (response.ok) {
                showMessage('success', 'Користувача успішно зареєстровано! Тепер можете увійти.');
                event.target.reset();
                setActiveTab('signin');
            } else {
                const errorData = await response.json().catch(() => ({ message: 'Сервер повернув не JSON відповідь.' }));
                showMessage('error', `Помилка реєстрації: ${errorData.message || 'Невідома помилка'}`);
                console.error('Помилка реєстрації:', errorData);
            }
        } catch (error) {
            showMessage('error', `Мережева помилка: ${error.message}`);
            console.error('Мережева помилка:', error);
        }
    };

    const handleSigninSubmit = async (event) => {
        event.preventDefault();
        setMessage({ type: '', text: '' });

        const username = event.target.signinUsername.value;
        const password = event.target.signinPassword.value;

        try {
            const response = await fetch(`${apiUrl}/api/auth/signin`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const data = await response.json();
                showMessage('success', 'Вхід успішний!');
                // ЗМІНЕНО: Тепер передаємо весь об'єкт data батьківському компоненту
                onLoginSuccess(data);
            } else {
                const errorData = await response.json().catch(() => ({ message: 'Сервер повернув не JSON відповідь.' }));
                showMessage('error', `Помилка входу: ${errorData.message || 'Невідома помилка'}`);
                console.error('Помилка входу:', errorData);
            }
        } catch (error) {
            showMessage('error', `Мережева помилка: ${error.message}`);
            console.error('Мережева помилка:', error);
        }
    };

    return (
        <div className="auth-container">
            <div className="tab-buttons">
                <button
                    className={`tab-button ${activeTab === 'signup' ? 'active' : ''}`}
                    onClick={() => setActiveTab('signup')}
                >
                    Реєстрація
                </button>
                <button
                    className={`tab-button ${activeTab === 'signin' ? 'active' : ''}`}
                    onClick={() => setActiveTab('signin')}
                >
                    Вхід
                </button>
            </div>

            {message.text && (
                <div className={`message ${message.type}`}>
                    {message.text}
                </div>
            )}

            {activeTab === 'signup' && (
                <div id="signupFormSection" className="form-section active">
                    <h2>Реєстрація нового користувача</h2>
                    <form id="signupForm" onSubmit={handleSignupSubmit}>
                        <div>
                            <label htmlFor="signupUsername">Ім'я користувача:</label>
                            <input type="text" id="signupUsername" name="username" required />
                        </div>
                        <div>
                            <label htmlFor="signupEmail">Email:</label>
                            <input type="email" id="signupEmail" name="email" required />
                        </div>
                        <div>
                            <label htmlFor="signupPassword">Пароль:</label>
                            <input type="password" id="signupPassword" name="password" required />
                        </div>
                        <div>
                            <label htmlFor="signupRoles">Ролі (через кому, наприклад: user,admin):</label>
                            <input type="text" id="signupRoles" name="roles" defaultValue="user" />
                        </div>
                        <button type="submit">Зареєструватися</button>
                    </form>
                </div>
            )}

            {activeTab === 'signin' && (
                <div id="signinFormSection" className="form-section active">
                    <h2>Вхід</h2>
                    <form id="signinForm" onSubmit={handleSigninSubmit}>
                        <div>
                            <label htmlFor="signinUsername">Ім'я користувача:</label>
                            <input type="text" id="signinUsername" name="username" required />
                        </div>
                        <div>
                            <label htmlFor="signinPassword">Пароль:</label>
                            <input type="password" id="signinPassword" name="password" required />
                        </div>
                        <button type="submit">Увійти</button>
                    </form>
                </div>
            )}
        </div>
    );
}

export default AuthForm;