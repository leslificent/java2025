const API_MOVIES_BASE = '/api/movies';

const yearFromInput = document.getElementById('yearFrom');
const yearToInput = document.getElementById('yearTo');
const scrapeButton = document.getElementById('scrapeButton');
const loadMoviesButton = document.getElementById('loadMoviesButton');
const downloadXlsxButton = document.getElementById('downloadXlsxButton');
const moviesTableBody = document.getElementById('moviesTableBody');
const loadingIndicator = document.getElementById('loadingIndicatorMovies');


function setLoadingMovies(isLoading) {
    loadingIndicator.style.display = isLoading ? 'block' : 'none';
}


function getYearRange() {
    const yearFrom = parseInt(yearFromInput.value, 10);
    const yearTo = parseInt(yearToInput.value, 10);
    const currentYear = new Date().getFullYear();

    if (isNaN(yearFrom) || isNaN(yearTo) || yearFrom < 1888 || yearTo > currentYear + 1 || yearFrom > yearTo) {
        alert('Будь ласка, введіть коректний діапазон років.');
        return null;
    }
    return { yearFrom, yearTo };
}


async function fetchAndDisplayMovies() {
    const range = getYearRange();
    if (!range) return;

    setLoadingMovies(true);
    moviesTableBody.innerHTML = '';

    try {
        const response = await fetch(`${API_MOVIES_BASE}/load?yearFrom=${range.yearFrom}&yearTo=${range.yearTo}`);
        if (!response.ok) {
            throw new Error(`Помилка мережі: ${response.status} ${response.statusText}`);
        }
        const movies = await response.json();
        displayMoviesInTable(movies);
    } catch (error) {
        console.error('Помилка при отриманні списку фільмів:', error);
        alert(`Не вдалося завантажити список фільмів: ${error.message}`);
        const row = moviesTableBody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 4; // Количество колонок
        cell.textContent = `Помилка завантаження: ${error.message}`;
    } finally {
        setLoadingMovies(false);
    }
}


async function scrapeAndRefresh() {
    const range = getYearRange();
    if (!range) return;

    setLoadingMovies(true);
    console.log(`Запуск скрапінга для діапазона: ${range.yearFrom}-${range.yearTo}`);

    try {
        const scrapeResponse = await fetch(`${API_MOVIES_BASE}/scrape`, {
            method: 'POST',
        });

        if (!scrapeResponse.ok) {
            let errorBody = '';
            try { errorBody = await scrapeResponse.text(); } catch(e) {}
            throw new Error(`Помилка при запуску скрапінга: ${scrapeResponse.status} ${scrapeResponse.statusText}. ${errorBody}`);
        }

        const processedMovies = await scrapeResponse.json();
        console.log('Скрапінг завершено на бекенді. Оброблено:', processedMovies);
        alert(`Оновлення завершено. Знайдено/оновлено ${processedMovies.length} фільмів.`);


    } catch (error) {
        console.error('Помилка в процесі скрапінга і оновлення:', error);
        alert(`Помилка: ${error.message}`);
        await fetchAndDisplayMovies();
    } finally {
        setLoadingMovies(false);
    }
}


function displayMoviesInTable(movies) {
    moviesTableBody.innerHTML = '';

    if (!movies || movies.length === 0) {
        const row = moviesTableBody.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 4; // Количество колонок
        cell.textContent = 'Фільми у вказаному діапазоні не знайдені або ще не завантажені.';
        return;
    }

    movies.forEach(movie => {
        const row = moviesTableBody.insertRow();
        row.insertCell().textContent = movie.title || '';
        row.insertCell().textContent = movie.releaseYear !== null ? movie.releaseYear : '';
        row.insertCell().textContent = movie.genres || '';
        row.insertCell().textContent = movie.id || '';
    });
}


function downloadMoviesXlsx() {
    const range = getYearRange();
    if (!range) return;

    const url = `${API_MOVIES_BASE}/export/xlsx?yearFrom=${range.yearFrom}&yearTo=${range.yearTo}`;
    console.log('Запит на завантаження Excel:', url);
    window.location.href = url;
}


scrapeButton.addEventListener('click', scrapeAndRefresh);
loadMoviesButton.addEventListener('click', fetchAndDisplayMovies);
downloadXlsxButton.addEventListener('click', downloadMoviesXlsx);
