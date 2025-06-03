import React, { useState, useEffect, useCallback } from 'react';
import EventList from './components/EventList';
import AddEventForm from './components/AddEventForm';
import AuthForm from './components/AuthForm';
import './App.css';
import './AuthForm.css';
import './components/EventList.css';

function App() {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [errorMessage, setErrorMessage] = useState('');
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [accessToken, setAccessToken] = useState(localStorage.getItem('accessToken') || null);
    const [currentUsername, setCurrentUsername] = useState(localStorage.getItem('username') || null);
    // Новий стан для події, яка редагується
    const [editingEvent, setEditingEvent] = useState(null);

    // Функція для обробки виходу
    const handleLogout = useCallback(() => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('username');
        setAccessToken(null);
        setCurrentUsername(null);
        setIsAuthenticated(false);
        setEvents([]);
        setErrorMessage('');
        setError(false);
        setEditingEvent(null); // Очистити стан редагування при виході
    }, []);

    // Функція для отримання подій
    const fetchEvents = useCallback(() => {
        if (!accessToken) {
            setError(true);
            setErrorMessage('Токен відсутній. Будь ласка, увійдіть, щоб отримати доступ до подій.');
            return;
        }

        setLoading(true);
        setError(null);
        setErrorMessage('');
        const apiUrl = 'http://localhost:8080';

        fetch(`${apiUrl}/api/events`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
        })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 401 || response.status === 403) {
                        handleLogout();
                        throw new Error('Помилка автентифікації або доступу до подій. Будь ласка, увійдіть знову.');
                    }
                    return response.json().then(err => {
                        throw new Error(`HTTP error! status: ${response.status}, message: ${err.message || 'Невідома помилка'}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                setEvents(data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Помилка при отриманні подій: ", err);
                setError(true);
                setErrorMessage(err.message);
                setLoading(false);
            });
    }, [accessToken, handleLogout]);

    // Перевіряємо токен та ім'я користувача при завантаженні компонента
    useEffect(() => {
        if (localStorage.getItem('accessToken') && localStorage.getItem('username')) {
            setIsAuthenticated(true);
            setAccessToken(localStorage.getItem('accessToken'));
            setCurrentUsername(localStorage.getItem('username'));
        } else {
            handleLogout();
        }
    }, [handleLogout]);

    // Ефект, який викликає fetchEvents, коли isAuthenticated стає true
    useEffect(() => {
        if (isAuthenticated) {
            fetchEvents();
        }
    }, [isAuthenticated, fetchEvents]);

    // Обробник успішного входу
    const handleLoginSuccess = (data) => {
        setAccessToken(data.accessToken);
        setCurrentUsername(data.username);
        setIsAuthenticated(true);
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('username', data.username);
        console.log('JWT та ім\'я користувача збережено:', data.accessToken, data.username);
    };

    // Функція, яка викликається EventList при натисканні "Видалити"
    const handleEventDeleted = useCallback(() => {
        fetchEvents(); // Перезавантажуємо список після видалення
        setEditingEvent(null); // Якщо видалили подію, яка редагувалась, скидаємо режим редагування
    }, [fetchEvents]);

    // Функція, яка викликається EventList при натисканні "Редагувати"
    const handleEditClick = useCallback((eventToEdit) => {
        setEditingEvent(eventToEdit); // Встановлюємо подію, яку редагуємо
        // Можливо, тут потрібно прокрутити до форми AddEventForm, якщо вона не на екрані
    }, []);

    // Функція, яка викликається AddEventForm після успішного оновлення
    const handleEventUpdated = useCallback(() => {
        setEditingEvent(null); // Скидаємо стан редагування
        fetchEvents(); // Перезавантажуємо список після оновлення
    }, [fetchEvents]);

    return (
        <div className="App">
            <header className="App-header">
                <h1>Мій додаток подій</h1>
                {isAuthenticated && (
                    <div className="user-info-and-logout">
                        <span className="welcome-message">Привіт, {currentUsername}!</span>
                        <button onClick={handleLogout} className="logout-button">Вийти</button>
                    </div>
                )}
            </header>
            <main>
                {!isAuthenticated ? (
                    <AuthForm onLoginSuccess={handleLoginSuccess} />
                ) : (
                    <>
                        <AddEventForm
                            onEventAdded={fetchEvents}
                            accessToken={accessToken}
                            editingEvent={editingEvent} // Передаємо подію для редагування
                            onEventUpdated={handleEventUpdated} // Функція для обробки оновлення
                            onCancelEdit={() => setEditingEvent(null)} // Додаємо можливість скасувати редагування
                        />
                        <EventList
                            events={events}
                            loading={loading}
                            error={error}
                            errorMessage={errorMessage}
                            onEventDeleted={handleEventDeleted} // Передаємо функцію видалення
                            onEditClick={handleEditClick} // Передаємо функцію для початку редагування
                            accessToken={accessToken} // Передаємо токен для EventList
                        />
                    </>
                )}
            </main>
        </div>
    );
}

export default App;