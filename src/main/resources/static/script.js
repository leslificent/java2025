const API_BASE = '/api/crypto';
const tableBody = document.getElementById('cryptoTableBody');
const loadingIndicator = document.createElement('p');

loadingIndicator.id = 'loadingIndicator';
loadingIndicator.textContent = 'Загрузка данных...';
loadingIndicator.style.display = 'none';

document.querySelector('h1').insertAdjacentElement('afterend', loadingIndicator);


async function fetchAndDisplayData() {
    setLoading(true);
    console.log('Запуск оновлення даних на бекенді...');
    try {
        const fetchResponse = await fetch(`${API_BASE}/fetch`);

        if (!fetchResponse.ok) {
            const errorText = await fetchResponse.text();
            console.error('Помилка під час запуску оновлення даних на бекенді:', fetchResponse.status, errorText);
            alert(`Помилка ${fetchResponse.status} під час запуску оновлення даних на сервері. ${errorText}`);
        } else {
            console.log('Бекенд успішно запустив/завершив оновлення даних.');
        }

        const allData = await fetchResponse.json();
        console.log('Дані для відображення отримані:', allData);
        displayDataInTable(allData);

    } catch (error) {
        console.error('Загальна помилка в fetchAndDisplayData:', error);
        alert(`Відбулася мережева чи інша помилка при отриманні даних: ${error.message}`);
        clearTable();
    } finally {
        setLoading(false);
    }
}


function displayDataInTable(cryptos) {
    clearTable();

    if (!cryptos || cryptos.length === 0) {
        const row = tableBody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 8;
        cell.textContent = 'Відсутні дані для відображення. Натисніть "Завантажити всю криптовалюту".';
        return;
    }

    cryptos.forEach(crypto => {
        const row = tableBody.insertRow();

        row.insertCell().textContent = crypto.id || '';
        row.insertCell().textContent = crypto.symbol || '';
        row.insertCell().textContent = crypto.name || '';
        row.insertCell().textContent = crypto.rank !== null ? crypto.rank : '';

        const priceUsdCell = row.insertCell();
        priceUsdCell.textContent = crypto.price_usd !== null ? parseFloat(crypto.price_usd).toFixed(4) : '';
        priceUsdCell.style.textAlign = 'right';

        const change24hCell = row.insertCell();
        change24hCell.textContent = crypto.percent_change_24h ? `${crypto.percent_change_24h}%` : '';
        if (crypto.percent_change_24h) {
            const change = parseFloat(crypto.percent_change_24h);
            change24hCell.style.color = change > 0 ? 'green' : (change < 0 ? 'red' : 'black');
        }
        change24hCell.style.textAlign = 'right';

        const marketCapCell = row.insertCell();
        marketCapCell.textContent = crypto.market_cap_usd !== null
            ? parseFloat(crypto.market_cap_usd).toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            })
            : '';
        marketCapCell.style.textAlign = 'right';

        const volume24Cell = row.insertCell();
        volume24Cell.textContent = crypto.volume24 !== null
            ? parseFloat(crypto.volume24).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2})
            : '';
        volume24Cell.style.textAlign = 'right';


    });
}


function clearTable() {
    if (tableBody) {
        tableBody.innerHTML = '';
    }
}


function setLoading(isLoading) {
    if (loadingIndicator) {
        loadingIndicator.style.display = isLoading ? 'block' : 'none';
    }
}


function downloadFile(url, filename) {
    console.log(`Запит на завантаження: ${filename} с ${url}`);
    window.location.href = url;
}

function downloadExcel() {
    downloadFile(`${API_BASE}/export/excel`, 'cryptocurrencies.xlsx');
}