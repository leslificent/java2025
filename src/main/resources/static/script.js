/**
 * Frontend JavaScript for Crypto Data Application
 */

// Базовый URL для API запросов на бэкенд
const API_BASE = '/api/crypto';
// Ссылка на тело таблицы для добавления данных
const tableBody = document.getElementById('cryptoTableBody');
// Элемент для отображения статуса загрузки (опционально)
const loadingIndicator = document.createElement('p'); // Создаем элемент для индикатора
loadingIndicator.id = 'loadingIndicator';
loadingIndicator.textContent = 'Загрузка данных...';
loadingIndicator.style.display = 'none'; // Скрыт по умолчанию
// Добавляем индикатор перед таблицей (или в другое удобное место)
document.querySelector('h1').insertAdjacentElement('afterend', loadingIndicator);


/**
 * Запускает процесс получения данных с API (через бэкенд)
 * и последующее отображение всех данных из БД в таблице.
 */
async function fetchAndDisplayData() {
    setLoading(true); // Показать индикатор загрузки
    console.log('Запуск обновления данных на бэкенде...');
    try {
        // 1. Отправляем запрос на бэкенд, чтобы он скачал свежие данные с CoinLore API
        const fetchResponse = await fetch(`${API_BASE}/fetch`);

        if (!fetchResponse.ok) {
            // Если бэкенд вернул ошибку при запросе к CoinLore
            const errorText = await fetchResponse.text(); // Попытаемся прочитать текст ошибки
            console.error('Ошибка при запуске обновления данных на бэкенде:', fetchResponse.status, errorText);
            alert(`Ошибка ${fetchResponse.status} при запуске обновления данных на сервере. ${errorText}`);
            // Не прерываем, попытаемся загрузить то, что есть в БД
        } else {
            console.log('Бэкенд успешно запустил/завершил обновление данных.');
            // Можно обработать ответ, если /fetch что-то возвращает
            // const savedData = await fetchResponse.json();
            // console.log('Ответ от /fetch:', savedData);
        }

        // 2. Запрашиваем ВСЕ данные, которые теперь есть в БД бэкенда
        console.log('Запрос всех данных из БД для отображения...');
        const allResponse = await fetch(`${API_BASE}/all`);
        if (!allResponse.ok) {
            const errorText = await allResponse.text();
            console.error('Ошибка при получении данных из БД:', allResponse.status, errorText);
            alert(`Ошибка ${allResponse.status} при получении данных для отображения. ${errorText}`);
            clearTable(); // Очищаем таблицу в случае ошибки
            setLoading(false); // Убрать индикатор загрузки
            return; // Прерываем выполнение
        }

        const allData = await allResponse.json();
        console.log('Данные для отображения получены:', allData);
        displayDataInTable(allData); // Отображаем данные в таблице

    } catch (error) {
        // Ловим ошибки сети или другие непредвиденные ошибки
        console.error('Общая ошибка в fetchAndDisplayData:', error);
        alert(`Произошла сетевая или другая ошибка при получении данных: ${error.message}`);
        clearTable();
    } finally {
        setLoading(false); // В любом случае убираем индикатор загрузки
    }
}

/**
 * Отображает массив данных о криптовалютах в HTML-таблице.
 * @param {Array<Object>} cryptos - Массив объектов криптовалют.
 */
function displayDataInTable(cryptos) {
    clearTable(); // Очищаем таблицу перед заполнением

    if (!cryptos || cryptos.length === 0) {
        const row = tableBody.insertRow();
        const cell = row.insertCell();
        // Убедитесь, что colspan соответствует количеству ваших заголовков
        cell.colSpan = 8; // Обновите, если добавили/удалили столбцы
        cell.textContent = 'Нет данных для отображения. Нажмите "Выгрузить всю криптовалюту".';
        return;
    }

    cryptos.forEach(crypto => {
        const row = tableBody.insertRow();

        // Добавляем ячейки для каждого поля. Используем || '' для пустых значений.
        // Форматируем числа при необходимости.
        row.insertCell().textContent = crypto.id || '';
        row.insertCell().textContent = crypto.symbol || '';
        row.insertCell().textContent = crypto.name || '';
        row.insertCell().textContent = crypto.rank !== null ? crypto.rank : ''; // rank может быть 0

        // Цена USD - форматируем до 4 знаков после запятой
        const priceUsdCell = row.insertCell();
        priceUsdCell.textContent = crypto.price_usd !== null ? parseFloat(crypto.price_usd).toFixed(4) : '';
        priceUsdCell.style.textAlign = 'right'; // Выравнивание по правому краю для чисел

        // Процент изменения за 24ч
        const change24hCell = row.insertCell();
        change24hCell.textContent = crypto.percent_change_24h ? `${crypto.percent_change_24h}%` : '';
        // Опционально: окрашивание в зависимости от знака
        if (crypto.percent_change_24h) {
            const change = parseFloat(crypto.percent_change_24h);
            change24hCell.style.color = change > 0 ? 'green' : (change < 0 ? 'red' : 'black');
        }
        change24hCell.style.textAlign = 'right';

        // Капитализация - форматируем с разделителями тысяч
        const marketCapCell = row.insertCell();
        marketCapCell.textContent = crypto.market_cap_usd !== null
            ? parseFloat(crypto.market_cap_usd).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
            : '';
        marketCapCell.style.textAlign = 'right';

        // Объем за 24ч - форматируем с разделителями тысяч
        const volume24Cell = row.insertCell();
        volume24Cell.textContent = crypto.volume24 !== null
            ? parseFloat(crypto.volume24).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
            : '';
        volume24Cell.style.textAlign = 'right';

        // Добавьте сюда ячейки для других полей, если они есть в таблице
        // Например:
        // row.insertCell().textContent = crypto.csupply || '';
    });
}

/**
 * Очищает содержимое тела таблицы.
 */
function clearTable() {
    if (tableBody) {
        tableBody.innerHTML = '';
    }
}

/**
 * Управляет видимостью индикатора загрузки.
 * @param {boolean} isLoading - true, чтобы показать индикатор, false - скрыть.
 */
function setLoading(isLoading) {
    if (loadingIndicator) {
        loadingIndicator.style.display = isLoading ? 'block' : 'none';
    }
}


/**
 * Инициирует скачивание файла с указанного URL.
 * @param {string} url - URL эндпоинта для скачивания файла.
 * @param {string} filename - Имя файла, которое будет предложено пользователю.
 */
function downloadFile(url, filename) {
    console.log(`Запрос на скачивание: ${filename} с ${url}`);
    // Самый простой способ - перенаправить браузер. Бэкенд должен выставить
    // заголовок Content-Disposition: attachment; filename="<filename>"
    window.location.href = url;
}

/**
 * Обработчик для кнопки "Скачать в Excel".
 */
function downloadExcel() {
    downloadFile(`${API_BASE}/export/excel`, 'cryptocurrencies.xlsx');
}

/**
 * Обработчик для кнопки "Скачать в CSV" (если она есть).
 */
function downloadCsv() {
    downloadFile(`${API_BASE}/export/csv`, 'cryptocurrencies.csv');
}

// --- Инициализация ---
// Опционально: Загрузить данные при первой загрузке страницы
// window.addEventListener('load', fetchAndDisplayData);

// Или можно оставить загрузку только по кнопке.
// Убедитесь, что кнопки в HTML имеют правильные атрибуты onclick:
// <button onclick="fetchAndDisplayData()">Выгрузить всю криптовалюту</button>
// <button onclick="downloadExcel()">Скачать в Excel</button>
// <button onclick="downloadCsv()">Скачать в CSV</button>