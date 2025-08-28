// Header scroll effect
window.addEventListener('scroll', function () {
    const header = document.querySelector('header');
    if (window.scrollY > 50) {
        header.classList.add('scrolled');
    } else {
        header.classList.remove('scrolled');
    }
});

// Mobile Navigation Menu Toggle
document.addEventListener('DOMContentLoaded', () => {
    const hamburgerBtn = document.querySelector('.hamburger-menu-button');
    const closeMenuBtn = document.querySelector('.close-menu-button');
    const mobileNavMenu = document.querySelector('.mobile-nav-menu');
    const mobileNavOverlay = document.querySelector('.mobile-nav-overlay');
    const mobileNavLinks = document.querySelectorAll('.mobile-nav-links ul li a');

    if (hamburgerBtn && mobileNavMenu && closeMenuBtn && mobileNavOverlay) {
        hamburgerBtn.addEventListener('click', () => {
            mobileNavMenu.classList.add('active');
            mobileNavOverlay.classList.add('active');
        });

        closeMenuBtn.addEventListener('click', () => {
            mobileNavMenu.classList.remove('active');
            mobileNavOverlay.classList.remove('active');
        });

        mobileNavOverlay.addEventListener('click', () => {
            mobileNavMenu.classList.remove('active');
            mobileNavOverlay.classList.remove('active');
        });

        mobileNavLinks.forEach(link => {
            link.addEventListener('click', () => {
                mobileNavMenu.classList.remove('active');
                mobileNavOverlay.classList.remove('active');
            });
        });
    }
});

// Update Data (Centralized for both pages)
const updateData = {
    update1: {
        image: "assets/update.jpg",
        headline: "Rhythm's Website Launch ;)",
        date: "August 27, 2025",
        writer: "Anjishnu Nandi",
        details: `
            <p>We're excited to announce the launch of our new website, designed to provide a better user experience and easier access to all things related to Rhythm.</p>
        `
    },
    update2: {
        image: "assets/Banner.png",
        headline: "2.7 Stable Update is here!",
        date: "August 17, 2025",
        writer: "Anjishnu Nandi",
        details: `
            <p>Greetings, Rhythm users! We're excited to bring you a maintenance update focused on enhancing your overall experience. This release addresses several key areas, bringing new features, crucial fixes, and performance improvements.</p>
            <h3>What's New:</h3>
            <ul>
                <li>Added: Play All & Mix to few Widgets (Home)</li>
                <li>Fixed: Crash due to "IllegalArgumentException: colors must have length of at least 2 if colorStops is omitted" (Player)</li>
                <li>Fixed: Tab Switch sometimes skipped Playlists Tab (Library)</li>
                <li>Improved: Tweaked color schemes for Controls based on their functions (Player)</li>
                <li>Improved: Filters (Library)</li>
                <li>Many more optimizations & improvements...</li>
            </ul>
            <h3>Known Issues (Will be fixed on a later build):</h3>
            <ul>
                <li>Few artist images not showing up</li>
                <li>Canvas needs more optimization</li>
                <li>Shuffle needs more improvement</li>
                <li>Reinstalling Rhythm skips On-Boarding and Media Scanning after launching</li>
            </ul>
            <h3>Build Information:</h3>
            <ul>
                <li>Build: 503</li>
                <li>Type: Stable Release.</li>
            </ul>
            <p>Thank you for your continued support!</p>
        `
    },
};

// Function to check if an image exists
function imageExists(url, callback) {
    const img = new Image();
    img.onload = function() { callback(true); };
    img.onerror = function() { callback(false); };
    img.src = url;
}

// News Carousel Functionality (for index.html)
function setupNewsCarousel() {
    const newsCarouselTrack = document.getElementById('dynamic-news-updates');
    const newsCarouselContainer = document.querySelector('.news-carousel');
    const newsPrevBtn = document.querySelector('.news-prev-btn');
    const newsNextBtn = document.querySelector('.news-next-btn');

    if (!newsCarouselTrack || !newsCarouselContainer || !newsPrevBtn || !newsNextBtn) {
        return; // Exit if news carousel elements are not found
    }

    // Clear existing content
    newsCarouselTrack.innerHTML = '';

    // Populate news items dynamically
    Object.keys(updateData).slice(0, 3).forEach(key => { // Show only the latest 3 updates
        const data = updateData[key];
        const newsItem = document.createElement('div');
        newsItem.className = 'news-carousel-item';
        newsItem.setAttribute('data-update-id', key);

        let imageHtml = `<img src="${data.image}" alt="Update Image">`;
        if (!data.image) {
            imageHtml = `<img src="../assets/icon.png" alt="Rhythm Logo" class="fallback-logo">`;
            newsItem.classList.add('no-image');
        }

        newsItem.innerHTML = `
            ${imageHtml}
            <div class="news-overlay">
                <h3>${data.headline}</h3>
                <p>${data.details.substring(0, 100)}...</p> <!-- Show a summary -->
                <a href="updates.html#${key}" class="btn btn-primary">Read More</a>
            </div>
        `;
        newsCarouselTrack.appendChild(newsItem);
    });

    const newsSlides = document.querySelectorAll('.news-carousel-item');
    let newsSlideIndex = 0;

    function getNewsSlidesPerView() {
        if (window.innerWidth >= 992) {
            return 3;
        } else if (window.innerWidth >= 768) {
            return 2;
        } else {
            return 1;
        }
    }

    function updateNewsCarousel() {
        const slidesPerView = getNewsSlidesPerView();
        const totalSlides = newsSlides.length;
        const slideWidth = newsCarouselContainer.offsetWidth / slidesPerView;

        newsSlides.forEach(slide => {
            slide.style.flex = `0 0 ${100 / slidesPerView}%`;
        });

        if (newsSlideIndex > totalSlides - slidesPerView && totalSlides >= slidesPerView) {
            newsSlideIndex = totalSlides - slidesPerView;
        } else if (newsSlideIndex < 0) {
            newsSlideIndex = 0;
        } else if (newsSlideIndex >= totalSlides) {
            newsSlideIndex = totalSlides - slidesPerView;
        }

        newsCarouselTrack.style.transform = `translateX(-${newsSlideIndex * slideWidth}px)`;
    }

    function showNewsSlide(index) {
        const slidesPerView = getNewsSlidesPerView();
        const totalSlides = newsSlides.length;

        newsSlideIndex = index;

        if (newsSlideIndex < 0) {
            newsSlideIndex = totalSlides - slidesPerView;
        } else if (newsSlideIndex > totalSlides - slidesPerView) {
            newsSlideIndex = 0;
        }
        updateNewsCarousel();
    }

    newsPrevBtn.addEventListener('click', () => {
        showNewsSlide(newsSlideIndex - 1);
    });

    newsNextBtn.addEventListener('click', () => {
        showNewsSlide(newsSlideIndex + 1);
    });

    window.addEventListener('resize', updateNewsCarousel);
    updateNewsCarousel(); // Initial update
    showNewsSlide(0); // Initialize to the first slide
}

// Showcase Carousel Functionality (for index.html)
function setupShowcaseCarousel() {
    const showcaseCarouselTrack = document.getElementById('showcase-carousel-track');
    const showcaseCarouselContainer = document.querySelector('.showcase-carousel');
    const showcasePrevBtn = document.querySelector('.showcase-prev-btn');
    const showcaseNextBtn = document.querySelector('.showcase-next-btn');

    if (!showcaseCarouselTrack || !showcaseCarouselContainer || !showcasePrevBtn || !showcaseNextBtn) {
        return; // Exit if showcase carousel elements are not found
    }

    const showcaseSlides = document.querySelectorAll('.showcase-carousel-item');
    let showcaseSlideIndex = 0;

    function getShowcaseSlidesPerView() {
        if (window.innerWidth >= 992) {
            return 3;
        } else if (window.innerWidth >= 768) {
            return 2;
        } else {
            return 1;
        }
    }

    function updateShowcaseCarousel() {
        const slidesPerView = getShowcaseSlidesPerView();
        const totalSlides = showcaseSlides.length;
        const slideWidth = showcaseCarouselContainer.offsetWidth / slidesPerView;

        showcaseSlides.forEach(slide => {
            slide.style.flex = `0 0 ${100 / slidesPerView}%`;
        });

        if (showcaseSlideIndex > totalSlides - slidesPerView && totalSlides >= slidesPerView) {
            showcaseSlideIndex = totalSlides - slidesPerView;
        } else if (showcaseSlideIndex < 0) {
            showcaseSlideIndex = 0;
        } else if (showcaseSlideIndex >= totalSlides) {
            showcaseSlideIndex = totalSlides - slidesPerView;
        }

        showcaseCarouselTrack.style.transform = `translateX(-${showcaseSlideIndex * slideWidth}px)`;
    }

    function showShowcaseSlide(index) {
        const slidesPerView = getShowcaseSlidesPerView();
        const totalSlides = showcaseSlides.length;

        showcaseSlideIndex = index;

        if (showcaseSlideIndex < 0) {
            showcaseSlideIndex = totalSlides - slidesPerView;
        } else if (showcaseSlideIndex > totalSlides - slidesPerView) {
            showcaseSlideIndex = 0;
        }
        updateShowcaseCarousel();
    }

    showcasePrevBtn.addEventListener('click', () => {
        showShowcaseSlide(showcaseSlideIndex - 1);
    });

    showcaseNextBtn.addEventListener('click', () => {
        showShowcaseSlide(showcaseSlideIndex + 1);
    });

    window.addEventListener('resize', updateShowcaseCarousel);
    updateShowcaseCarousel(); // Initial update
    showShowcaseSlide(0); // Initialize to the first slide
}

// Update Popup Functionality (for updates.html)
function setupUpdatePopup() {
    const updateItems = document.querySelectorAll('.updates-list .update-item');
    const updatePopup = document.getElementById('update-popup');
    const closePopupBtn = document.querySelector('.close-popup-btn');
    const popupImage = document.getElementById('popup-image');
    const popupHeadline = document.getElementById('popup-headline');
    const popupDate = document.getElementById('popup-date');
    const popupWriter = document.getElementById('popup-writer');
    const popupDetails = document.getElementById('popup-details');

    if (!updatePopup) return; // Exit if not on updates.html

    updateItems.forEach(item => {
        const updateId = item.getAttribute('data-update-id');
        const data = updateData[updateId];
        const itemImage = item.querySelector('img');

        if (data && data.image) {
            imageExists(`${data.image}`, (exists) => { // Path is relative to updates.html
                if (!exists) {
                    item.classList.add('no-image');
                    itemImage.insertAdjacentHTML('afterend', `<img src="assets/icon.png" alt="Rhythm Logo" class="fallback-logo">`);
                }
            });
        } else {
            item.classList.add('no-image');
            itemImage.insertAdjacentHTML('afterend', `<img src="assets/icon.png" alt="Rhythm Logo" class="fallback-logo">`);
        }

        item.addEventListener('click', () => {
            if (data) {
                if (data.image) {
                    imageExists(`${data.image}`, (exists) => { // Path is relative to updates.html
                        if (exists) {
                            popupImage.src = `${data.image}`;
                        } else {
                            popupImage.src = "assets/icon.png"; // Fallback to app logo
                        }
                    });
                } else {
                    popupImage.src = "assets/icon.png"; // Fallback to app logo
                }
                
                popupHeadline.textContent = data.headline;
                popupDate.textContent = data.date;
                popupWriter.textContent = data.writer;
                popupDetails.innerHTML = data.details;
                updatePopup.classList.add('active');
            }
        });
    });

    closePopupBtn.addEventListener('click', () => {
        updatePopup.classList.remove('active');
    });

    updatePopup.addEventListener('click', (e) => {
        if (e.target === updatePopup) {
            updatePopup.classList.remove('active');
        }
    });
}


// Smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();

        const targetId = this.getAttribute('href');
        if (targetId === '#') {
            // For the logo, scroll to the absolute top
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        } else {
            const target = document.querySelector(targetId);
            if (target) {
                window.scrollTo({
                    top: target.offsetTop - 80,
                    behavior: 'smooth'
                });
            }
        }
    });
});


// Animation on scroll
function setupScrollAnimations() {
    const animateElements = document.querySelectorAll('.feature-card, .section-header, .dashboard-preview');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.animation = 'fadeIn 0.6s forwards';
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    animateElements.forEach(element => {
        element.style.opacity = '0';
        observer.observe(element);
    });
}

// Dark mode toggle
function setupDarkModeToggle() {
    const footerSection = document.querySelector('.footer-section:first-child');
    const darkModeToggle = document.createElement('div');
    darkModeToggle.className = 'dark-mode-toggle';
    darkModeToggle.innerHTML = `
        <label class="switch">
            <input type="checkbox" id="darkModeSwitch">
            <span class="slider"></span>
        </label>
        <span>Dark Mode</span>
    `;

    // Insert styles for the toggle
    const styleEl = document.createElement('style');
    styleEl.textContent = `
        .dark-mode-toggle {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-top: 20px;
        }
        .switch {
            position: relative;
            display: inline-block;
            width: 60px;
            height: 30px;
        }
        .switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }
        .slider {
            position: absolute;
            cursor: pointer;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: #ccc;
            transition: .4s;
            border-radius: 34px;
        }
        .slider:before {
            position: absolute;
            content: "";
            height: 22px;
            width: 22px;
            left: 4px;
            bottom: 4px;
            background-color: white;
            transition: .4s;
            border-radius: 50%;
        }
        input:checked + .slider {
            background-color: var(--primary); /* Ensure dark mode toggle uses primary color */
        }
        input:checked + .slider:before {
            transform: translateX(30px);
        }
    `;
    document.head.appendChild(styleEl);

    footerSection.appendChild(darkModeToggle);

    const darkModeSwitch = document.getElementById('darkModeSwitch');

    // Check for saved preference
    if (localStorage.getItem('darkMode') === 'enabled') {
        document.body.classList.add('dark-mode');
        darkModeSwitch.checked = true;
    }

    darkModeSwitch.addEventListener('change', () => {
        if (darkModeSwitch.checked) {
            document.body.classList.add('dark-mode');
            localStorage.setItem('darkMode', 'enabled');
        } else {
            document.body.classList.remove('dark-mode');
            localStorage.setItem('darkMode', null);
        }
    });
}

// Preloader
function setupPreloader() {
    const preloader = document.createElement('div');
    preloader.className = 'preloader';
    preloader.innerHTML = `
        <div class="spinner">
            <i class="fa-solid fa-arrows-rotate fa-spin"></i>
        </div>
    `;

    // Insert styles for the preloader
    const styleEl = document.createElement('style');
    styleEl.textContent = `
        .preloader {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: var(--light);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
            transition: opacity 0.5s ease-out, visibility 0.5s ease-out;
        }
        .spinner {
            font-size: 80px;
            color: var(--primary);
            // animation: pulse 1.5s infinite;
        }
        @keyframes pulse {
            0% { transform: scale(0.9); opacity: 0.7; }
            50% { transform: scale(1.1); opacity: 1; }
            100% { transform: scale(0.9); opacity: 0.7; }
        }
        .preloader.hidden {
            opacity: 0;
            visibility: hidden;
        }
    `;
    document.head.appendChild(styleEl);

    document.body.prepend(preloader);

    window.addEventListener('load', () => {
        setTimeout(() => {
            preloader.classList.add('hidden');
        }, 300);
    });
}

// Initialize all functionality
document.addEventListener('DOMContentLoaded', () => {
    setupScrollAnimations();
    setupDarkModeToggle();
    setupPreloader();

    const currentPage = window.location.pathname.split('/').pop();

    if (currentPage === 'index.html' || currentPage === '') {
        setupNewsCarousel(); // Initialize news carousel functionality for index.html
        setupShowcaseCarousel(); // Initialize showcase carousel functionality for index.html
    } else if (currentPage === 'updates.html') {
        setupUpdatePopup(); // Initialize update popup functionality for updates.html
        setupUpdateViewToggle(); // Initialize update view toggle functionality for updates.html
    }
});

// Update View Toggle Functionality (for updates.html)
function setupUpdateViewToggle() {
    const listViewBtn = document.getElementById('listViewBtn');
    const gridViewBtn = document.getElementById('gridViewBtn');
    const updatesList = document.querySelector('.updates-list');

    if (!listViewBtn || !gridViewBtn || !updatesList) {
        return; // Exit if elements are not found
    }

    // Set default view to list view
    updatesList.classList.add('list-view');
    listViewBtn.classList.add('btn-primary', 'active');
    gridViewBtn.classList.remove('btn-primary', 'active');
    gridViewBtn.classList.add('btn-outline');

    listViewBtn.addEventListener('click', () => {
        updatesList.classList.remove('grid-view');
        updatesList.classList.add('list-view');
        listViewBtn.classList.add('btn-primary', 'active');
        listViewBtn.classList.remove('btn-outline');
        gridViewBtn.classList.remove('btn-primary', 'active');
        gridViewBtn.classList.add('btn-outline');
    });

    gridViewBtn.addEventListener('click', () => {
        updatesList.classList.remove('list-view');
        updatesList.classList.add('grid-view');
        gridViewBtn.classList.add('btn-primary', 'active');
        gridViewBtn.classList.remove('btn-outline');
        listViewBtn.classList.remove('btn-primary', 'active');
        listViewBtn.classList.add('btn-outline');
    });
}

// Smooth scrolling for all download buttons
document.querySelectorAll('.scroll-to-download').forEach(button => {
    button.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            window.scrollTo({
                top: target.offsetTop - 80, // Adjust for fixed header
                behavior: 'smooth'
            });
        }
    });
});
