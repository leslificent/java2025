/**
 * Frontend JavaScript for Movie Data Page
 */

const API_MOVIES_BASE = '/api/movies';

// Получаем элементы управления
const yearFromInput = document.getElementById('yearFrom');
const yearToInput = document.getElementById('yearTo');
const scrapeButton = document.getElementById('scrapeButton');
const loadMoviesButton = document.getElementById('loadMoviesButton'); // Получаем новую кнопку
const downloadCsvButton = document.getElementById('downloadCsvButton');
const moviesTableBody = document.getElementById('moviesTableBody');
const loadingIndicator = document.getElementById('loadingIndicatorMovies');

// --- Функции ---

/**
 * Управляет видимостью индикатора загрузки.
 */
function setLoadingMovies(isLoading) {
    loadingIndicator.style.display = isLoading ? 'block' : 'none';
}

/**
 * Получает значения годов из полей ввода.
 * @returns {object} Объект { yearFrom, yearTo } или null при ошибке.
 */
function getYearRange() {
    const yearFrom = parseInt(yearFromInput.value, 10);
    const yearTo = parseInt(yearToInput.value, 10);
    const currentYear = new Date().getFullYear();

    // Простая валидация
    if (isNaN(yearFrom) || isNaN(yearTo) || yearFrom < 1888 || yearTo > currentYear + 1 || yearFrom > yearTo) {
        alert('Пожалуйста, введите корректный диапазон годов.');
        return null;
    }
    return { yearFrom, yearTo };
}

/**
 * Запрашивает данные с бэкенда и отображает их в таблице.
 */
async function fetchAndDisplayMovies() {
    const range = getYearRange();
    if (!range) return; // Выход если годы некорректны

    setLoadingMovies(true);
    moviesTableBody.innerHTML = ''; // Очищаем таблицу

    try {
        const response = await fetch(`${API_MOVIES_BASE}/load?yearFrom=${range.yearFrom}&yearTo=${range.yearTo}`);
        if (!response.ok) {
            throw new Error(`Ошибка сети: ${response.status} ${response.statusText}`);
        }
        const movies = await response.json();
        displayMoviesInTable(movies);
    } catch (error) {
        console.error('Ошибка при получении списка фильмов:', error);
        alert(`Не удалось загрузить список фильмов: ${error.message}`);
        // Показываем сообщение об ошибке в таблице
        const row = moviesTableBody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 4; // Количество колонок
        cell.textContent = `Ошибка загрузки: ${error.message}`;
    } finally {
        setLoadingMovies(false);
    }
}

/**
 * Запускает процесс скрапинга на бэкенде, а затем обновляет таблицу.
 */
async function scrapeAndRefresh() {
    const range = getYearRange();
    if (!range) return;

    setLoadingMovies(true);
    console.log(`Запуск скрапинга для диапазона: ${range.yearFrom}-${range.yearTo}`);

    try {
        // Отправляем POST запрос для запуска скрапинга
        const scrapeResponse = await fetch(`${API_MOVIES_BASE}/scrape?yearFrom=${range.yearFrom}&yearTo=${range.yearTo}`, {
            method: 'POST',
        });

        if (!scrapeResponse.ok) {
            // Попытка прочитать тело ошибки, если оно есть
            let errorBody = '';
            try { errorBody = await scrapeResponse.text(); } catch(e) {/* ignore */}
            throw new Error(`Ошибка при запуске скрапинга: ${scrapeResponse.status} ${scrapeResponse.statusText}. ${errorBody}`);
        }

        const processedMovies = await scrapeResponse.json();
        console.log('Скрапинг завершен на бэкенде. Обработано:', processedMovies);
        alert(`Обновление завершено. Найдено/обновлено ${processedMovies.length} фильмов.`);

        // После успешного скрапинга, можно автоматически обновить таблицу, если нужно
        // await fetchAndDisplayMovies();

    } catch (error) {
        console.error('Ошибка в процессе скрапинга и обновления:', error);
        alert(`Ошибка: ${error.message}`);
        // Можно попробовать загрузить данные, которые уже есть в БД
        await fetchAndDisplayMovies(); // Загружаем то, что есть
    } finally {
        setLoadingMovies(false); // Убираем индикатор в любом случае
    }
}


/**
 * Отображает массив фильмов в таблице.
 */
function displayMoviesInTable(movies) {
    moviesTableBody.innerHTML = ''; // Очищаем перед заполнением

    if (!movies || movies.length === 0) {
        const row = moviesTableBody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 4; // Количество колонок
        cell.textContent = 'Фильмы в указанном диапазоне не найдены или еще не загружены.';
        return;
    }

    movies.forEach(movie => {
        const row = moviesTableBody.insertRow();
        row.insertCell().textContent = movie.title || '';
        row.insertCell().textContent = movie.releaseYear !== null ? movie.releaseYear : '';
        row.insertCell().textContent = movie.genres || ''; // Отображаем жанры
        row.insertCell().textContent = movie.id || ''; // ID из БД
    });
}

/**
 * Инициирует скачивание CSV файла с отфильтрованными данными.
 */
function downloadMoviesCsv() {
    const range = getYearRange();
    if (!range) return;

    const url = `${API_MOVIES_BASE}/export/csv?yearFrom=${range.yearFrom}&yearTo=${range.yearTo}`;
    console.log('Запрос на скачивание CSV:', url);
    // Простой способ инициировать скачивание через перенаправление
    window.location.href = url;
}


// --- Привязка обработчиков событий ---
scrapeButton.addEventListener('click', scrapeAndRefresh);
loadMoviesButton.addEventListener('click', fetchAndDisplayMovies); // Обработчик для новой кнопки
downloadCsvButton.addEventListener('click', downloadMoviesCsv);

// --- Инициализация ---
// Загрузить данные при первой загрузке страницы (можно убрать, если не нужно при загрузке)
// window.addEventListener('load', fetchAndDisplayMovies);