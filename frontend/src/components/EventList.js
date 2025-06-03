import React from 'react';
import './EventList.css';

// Тепер EventList приймає accessToken та функції для обробки подій
function EventList({ events, loading, error, errorMessage, onEventDeleted, onEditClick, accessToken }) {

    if (loading) {
        return <div className="loading-message">Завантаження подій...</div>;
    }

    if (error) {
        return <div className="error-message">Помилка при завантаженні подій: {errorMessage}</div>;
    }

    // Функція для видалення події
    const handleDelete = async (id) => {
        const confirmDelete = window.confirm("Ви впевнені, що хочете видалити цю подію?");
        if (!confirmDelete) {
            return;
        }

        // Перевіряємо наявність токена перед відправкою запиту
        if (!accessToken) {
            alert('Для видалення події потрібна автентифікація. Будь ласка, увійдіть.');
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/events/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${accessToken}`, // ВИКОРИСТОВУЄМО ПЕРЕДАНИЙ ТОКЕН
                },
            });

            if (!response.ok) {
                // Якщо токен недійсний або немає дозволу
                if (response.status === 401 || response.status === 403) {
                    throw new Error('Не авторизовано або недостатньо прав для видалення події.');
                }
                const errorData = await response.json();
                throw new Error(errorData.message || 'Не вдалося видалити подію');
            }

            // Якщо видалення успішне, викликаємо функцію з батьківського компонента
            if (onEventDeleted) {
                onEventDeleted(); // Тепер не передаємо ID, просто вказуємо, що потрібно оновити список
            }
            alert("Подію успішно видалено!");
        } catch (err) {
            console.error("Помилка видалення події:", err);
            alert(`Помилка видалення події: ${err.message}`);
        }
    };

    return (
        <div className="event-list-container">
            <h2>Список подій</h2>
            {events.length > 0 ? (
                <ul>
                    {events.map(event => (
                        <li key={event.id} className="event-list-item">
                            <div>
                                <h3>{event.name}</h3>
                                {event.description && <p>{event.description}</p>}
                                <p><strong>Дата:</strong> {new Date(event.date).toLocaleDateString()}</p>
                                {event.location && <p><strong>Місцезнаходження:</strong> {event.location}</p>}
                                {event.eventType && <p><strong>Тип події:</strong> {event.eventType}</p>}
                            </div>
                            <div className="event-actions">
                                <button className="edit-button" onClick={() => onEditClick(event)}>Редагувати (PUT)</button>
                                <button className="delete-button" onClick={() => handleDelete(event.id)}>Видалити (DELETE)</button>
                            </div>
                        </li>
                    ))}
                </ul>
            ) : (
                <div className="loading-message">Немає доступних подій.</div>
            )}
        </div>
    );
}

export default EventList;