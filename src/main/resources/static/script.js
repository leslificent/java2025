// Function to load bank data based on the selected year
// Function to load bank data based on the selected year
function loadBanksData() {
    // Получаем выбранный год
    const year = document.getElementById('yearSelect').value;

    // Формируем URL для запроса
    const url = `http://localhost:8080/api/listings/load-from-api?year=${year}`;

    // Отправляем запрос на сервер
    fetch(url)
        .then(response => response.json())
        .then(data => {
            // Очистка таблицы
            const tableBody = document.getElementById('bankTableBody');
            tableBody.innerHTML = ''; // Очищаем таблицу перед добавлением новых данных

            // Заполнение таблицы данными
            data.forEach(bank => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${bank.regnum}</td>
                    <td>${bank.name}</td>
                    <td>${bank.shortname || 'N/A'}</td> <!-- Shortname -->
                    <td>${bank.fullName}</td>
                    <td>${bank.address || 'N/A'}</td> <!-- Address -->
                    <td>${bank.country}</td>
                    <td>${bank.bankGroup || 'N/A'}</td> <!-- Bank Group -->
                    <td>${bank.mfo || 'N/A'}</td> <!-- MFO -->
                    <td>${bank.type || 'N/A'}</td> <!-- Type -->
                    <td>${bank.description || 'N/A'}</td> <!-- Description -->
                    <td>${bank.value.toFixed(2) || '0.00'}</td> <!-- Value, formatted to 2 decimals -->
                    <td>${bank.date || 'N/A'}</td> <!-- Date -->
                    <td>${bank.tzep || 'N/A'}</td> <!-- Tzep -->
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Ошибка при загрузке данных банков:', error);
        });
}


// Для обработки события изменения года
document.getElementById('yearSelect').addEventListener('change', loadBanksData);

// Загружаем данные при начальной загрузке страницы
window.onload = loadBanksData;

// Existing scrapeData function
function scrapeData() {
    fetch('http://localhost:8080/api/listings/scrape')
        .then(response => response.text())
        .then(data => alert(data))
        .catch(error => console.error('Ошибка при сборе данных:', error));
}

// Existing fetchListings function
function fetchListings() {
    fetch('http://localhost:8080/api/listings')
        .then(response => {
            // Check if the response is OK (status 200)
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // Ensure data is in the expected format
            if (Array.isArray(data)) {
                let list = document.getElementById("list");
                list.innerHTML = ""; // Clear the previous list

                data.forEach(item => {
                    // Check that each item has the expected properties
                    if (item.title && item.price) {
                        let li = document.createElement("li");
                        li.innerText = `${item.title} - ${item.price}`;
                        list.appendChild(li);
                    } else {
                        console.warn('Item data is incomplete:', item);
                    }
                });
            } else {
                console.error('Unexpected data format:', data);
                displayErrorMessage('Unexpected data format.');
            }
        })
        .catch(error => {
            console.error('Error loading listings:', error);
            // Show error message on the page
            displayErrorMessage('Failed to load listings. Please try again later.');
        });
}

// Function to display error messages
function displayErrorMessage(message) {
    // Find or create the error message element
    let errorMessageElement = document.getElementById("error-message");
    if (!errorMessageElement) {
        errorMessageElement = document.createElement("div");
        errorMessageElement.id = "error-message";
        document.body.appendChild(errorMessageElement); // Append to the body or a specific container
    }

    errorMessageElement.style.color = "red"; // Set error text color to red
    errorMessageElement.innerText = message; // Display the message
}
