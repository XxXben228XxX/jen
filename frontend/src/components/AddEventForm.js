import React, { useState, useEffect } from 'react';
import './AddEventForm.css';
// AddEventForm тепер приймає editingEvent (для редагування)
// та onEventUpdated (для сповіщення про оновлення)
function AddEventForm({ onEventAdded, accessToken, editingEvent, onEventUpdated, onCancelEdit }) {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [date, setDate] = useState('');
    const [location, setLocation] = useState('');
    const [eventType, setEventType] = useState('');
    const [formError, setFormError] = useState('');
    const [isEditing, setIsEditing] = useState(false); // Новий стан для визначення режиму

    // Ефект для заповнення форми, якщо ми в режимі редагування
    useEffect(() => {
        if (editingEvent) {
            setName(editingEvent.name || '');
            setDescription(editingEvent.description || '');
            // Форматування дати для поля input type="date" (YYYY-MM-DD)
            setDate(editingEvent.date ? new Date(editingEvent.date).toISOString().split('T')[0] : '');
            setLocation(editingEvent.location || '');
            setEventType(editingEvent.eventType || '');
            setIsEditing(true);
        } else {
            // Очистити форму, якщо ми не в режимі редагування
            setName('');
            setDescription('');
            setDate('');
            setLocation('');
            setEventType('');
            setIsEditing(false);
            setFormError(''); // Очистити помилки при перемиканні режимів
        }
    }, [editingEvent]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setFormError('');

        if (!name || !date) {
            setFormError('Назва та дата є обов\'язковими полями!');
            return;
        }

        if (!accessToken) {
            setFormError('Ви не авторизовані. Будь ласка, увійдіть, щоб додати/редагувати подію.');
            return;
        }

        const eventData = {
            name,
            description,
            date,
            location,
            eventType,
        };

        const apiUrl = 'http://localhost:8080/api/events';
        const method = isEditing ? 'PUT' : 'POST';
        const url = isEditing ? `${apiUrl}/${editingEvent.id}` : apiUrl;

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${accessToken}`,
                },
                body: JSON.stringify(eventData),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `Не вдалося ${isEditing ? 'оновити' : 'додати'} подію`);
            }

            // Очистити форму після успішного додавання/оновлення
            setName('');
            setDescription('');
            setDate('');
            setLocation('');
            setEventType('');
            setFormError('');

            if (isEditing && onEventUpdated) {
                onEventUpdated(); // Сповіщаємо батьківський компонент про оновлення
                alert('Подію успішно оновлено!');
            } else if (onEventAdded) {
                onEventAdded(); // Сповіщаємо батьківський компонент про додавання
                alert('Подію успішно додано!');
            }
        } catch (err) {
            console.error(`Помилка при ${isEditing ? 'оновленні' : 'додаванні'} події:`, err);
            setFormError(`Помилка: ${err.message}`);
        }
    };

    return (
        <div className="add-event-form-container">
            <h2>{isEditing ? 'Редагувати подію' : 'Додати нову подію'}</h2>
            <form onSubmit={handleSubmit} className="add-event-form">
                {formError && <p className="form-error-message">{formError}</p>}
                <div className="form-group">
                    <label htmlFor="name">Назва події:</label>
                    <input
                        type="text"
                        id="name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="description">Опис:</label>
                    <textarea
                        id="description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        rows="3"
                    ></textarea>
                </div>
                <div className="form-group">
                    <label htmlFor="date">Дата:</label>
                    <input
                        type="date"
                        id="date"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="location">Місцезнаходження:</label>
                    <input
                        type="text"
                        id="location"
                        value={location}
                        onChange={(e) => setLocation(e.target.value)}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="eventType">Тип події:</label>
                    <input
                        type="text"
                        id="eventType"
                        value={eventType}
                        onChange={(e) => setEventType(e.target.value)}
                    />
                </div>
                <div className="form-actions">
                    <button type="submit" className="submit-button">
                        {isEditing ? 'Оновити подію' : 'Додати подію'}
                    </button>
                    {isEditing && (
                        <button type="button" onClick={onCancelEdit} className="cancel-button">
                            Скасувати редагування
                        </button>
                    )}
                </div>
            </form>
        </div>
    );
}

export default AddEventForm;