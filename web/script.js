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
    update3: {
        image: "assets/updates2.png",
        headline: "October 2025 Update: Major Improvements In Development!",
        date: "October 5, 2025",
        writer: "Anjishnu Nandi",
        details: `
            <p>Hey there, Rhythm community! We're thrilled to share our ongoing development progress with you. Since our solid 2.7 stable release, we've been pushing boundaries and adding features that will elevate your music experience to new heights.</p>
            <h3>What's Currently In Development:</h3>
            <h4>🎵 New Features & Enhancements:</h4>
            <ul>
                <li><strong>Sleep Timer</strong> - Customizable sleep timers with flexible duration options</li>
                <li><strong>Audio Equalizer</strong> - Professional-grade equalizer for fine-tuning your sound</li>
                <li><strong>Playlist Management</strong> - Advanced playlist import/export capabilities with backup support</li>
                <li><strong>Library Redesign</strong> - Complete library overhaul with single card layout and improved navigation</li>
                <li><strong>Metadata Editing</strong> - Edit song metadata including artwork, with permission handling</li>
                <li><strong>Genre-Based Filtering</strong> - Smart genre detection and filtering throughout the app</li>
                <li><strong>File Explorer</strong> - New Explorer tab with folder-based song grouping and storage access</li>
                <li><strong>Theme Customization</strong> - Custom color schemes, fonts, and theme previews</li>
                <li><strong>Enhanced Navigation</strong> - Improved navigation with customizable library tab order</li>
                <li><strong>Device Output Management</strong> - Better audio device switching and output handling</li>
                <li><strong>Cache & Performance</strong> - Advanced cache management and performance optimizations</li>
            </ul>
            <h4>🔧 Technical Improvements:</h4>
            <ul>
                <li>Enhanced lyrics fetching with better error handling</li>
                <li>Robust genre detection and UI integration</li>
                <li>Album art color extraction for dynamic theming</li>
                <li>Improved search functionality across settings</li>
                <li>Enhanced haptics and accessibility features</li>
                <li>Mobile-responsive navigation and UI improvements</li>
            </ul>
            <h3>🚀 Our Commitment to Progress:</h3>
            <p>We're diligently working on bringing all these features together into a comprehensive, stable release. Our focus remains on maintaining the stability you expect while pushing the boundaries of what's possible with a Material You music player.</p>
            <h3>🎯 Development Priorities:</h3>
            <ul>
                <li>Testing and refining all new features for stability</li>
                <li>Performance optimizations across the entire application</li>
                <li>Bug fixes and user experience improvements based on feedback</li>
                <li>Enhanced stability through rigorous testing</li>
                <li>Adding customization options to make Rhythm truly yours</li>
            </ul>
            <p><strong>Transparent Development:</strong> We believe in keeping our community informed about our progress. While we don't have an exact release date yet, we wanted to share what's currently taking shape. Thank you for your continued support and patience—your feedback helps shape the direction of Rhythm's development!</p>
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

    // Auto-scroll functionality for news carousel
    let newsAutoScrollInterval;
    function startNewsAutoScroll() {
        newsAutoScrollInterval = setInterval(() => {
            showNewsSlide(newsSlideIndex + 1);
        }, 5000); // Change slide every 5 seconds
    }

    function stopNewsAutoScroll() {
        clearInterval(newsAutoScrollInterval);
    }

    // Pause auto-scroll on hover
    newsCarouselContainer.addEventListener('mouseenter', stopNewsAutoScroll);
    newsCarouselContainer.addEventListener('mouseleave', startNewsAutoScroll);

    // Restart auto-scroll when manually navigating
    newsPrevBtn.addEventListener('click', () => {
        stopNewsAutoScroll();
        startNewsAutoScroll();
    });
    newsNextBtn.addEventListener('click', () => {
        stopNewsAutoScroll();
        startNewsAutoScroll();
    });

    startNewsAutoScroll(); // Start auto-scrolling on load
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

    // Auto-scroll functionality for showcase carousel
    let showcaseAutoScrollInterval;
    function startShowcaseAutoScroll() {
        showcaseAutoScrollInterval = setInterval(() => {
            showShowcaseSlide(showcaseSlideIndex + 1);
        }, 5000); // Change slide every 5 seconds
    }

    function stopShowcaseAutoScroll() {
        clearInterval(showcaseAutoScrollInterval);
    }

    // Pause auto-scroll on hover
    showcaseCarouselContainer.addEventListener('mouseenter', stopShowcaseAutoScroll);
    showcaseCarouselContainer.addEventListener('mouseleave', startShowcaseAutoScroll);

    // Restart auto-scroll when manually navigating
    showcasePrevBtn.addEventListener('click', () => {
        stopShowcaseAutoScroll();
        startShowcaseAutoScroll();
    });
    showcaseNextBtn.addEventListener('click', () => {
        stopShowcaseAutoScroll();
        startShowcaseAutoScroll();
    });

    startShowcaseAutoScroll(); // Start auto-scrolling on load
}

// Screenshot Popup Functionality (for index.html)
function setupScreenshotPopup() {
    const screenshotPopup = document.getElementById('screenshot-popup');
    const screenshotPopupImage = document.getElementById('screenshot-popup-image');
    const screenshotTitle = document.getElementById('screenshot-title');
    const screenshotCounter = document.getElementById('screenshot-counter');
    const closeScreenshotPopupBtn = document.querySelector('.close-screenshot-popup-btn');
    const zoomInBtn = document.getElementById('zoom-in-btn');
    const zoomOutBtn = document.getElementById('zoom-out-btn');
    const zoomResetBtn = document.getElementById('zoom-reset-btn');
    const zoomLevelDisplay = document.getElementById('zoom-level');
    const screenshotPrevBtn = document.getElementById('screenshot-prev-btn');
    const screenshotNextBtn = document.getElementById('screenshot-next-btn');

    if (!screenshotPopup || !screenshotPopupImage || !closeScreenshotPopupBtn) {
        return; // Exit if screenshot popup elements are not found
    }

    // Get all showcase carousel items
    const showcaseSlides = document.querySelectorAll('.showcase-carousel-item img');
    let currentScreenshotIndex = 0;
    let currentZoom = 1;
    const minZoom = 0.5;
    const maxZoom = 3.0;
    const zoomStep = 0.25;
    let isPanning = false;
    let startX, startY, initialX, initialY;

    // Function to get screenshot title from alt text or filename
    function getScreenshotTitle(imgElement) {
        const altText = imgElement.alt;
        const src = imgElement.src;
        const filename = src.split('/').pop().replace('.png', '').replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());

        // Use alt text if it exists and is not generic, otherwise format filename
        if (altText && altText !== 'Screenshot' && altText.includes('Screen')) {
            return altText;
        } else {
            return filename;
        }
    }

    // Function to update zoom level display
    function updateZoomDisplay() {
        const percentage = Math.round(currentZoom * 100);
        zoomLevelDisplay.textContent = `${percentage}%`;
    }

    // Function to apply zoom to image
    function applyZoom(zoomValue) {
        screenshotPopupImage.style.transform = `scale(${zoomValue})`;
        updateZoomDisplay();
    }

    // Function to zoom in
    function zoomIn() {
        if (currentZoom < maxZoom) {
            currentZoom = Math.min(currentZoom * 1.5, maxZoom);
            applyZoom(currentZoom);
        }
    }

    // Function to zoom out
    function zoomOut() {
        if (currentZoom > minZoom) {
            currentZoom = Math.max(currentZoom * 0.75, minZoom);
            applyZoom(currentZoom);
        }
    }

    // Function to reset zoom and position
    function resetZoom() {
        currentZoom = 1;
        screenshotPopupImage.style.transform = 'scale(1) translate(0, 0)';
        updateZoomDisplay();
    }

    // Function to show screenshot popup
    function showScreenshotPopup(index) {
        if (index >= 0 && index < showcaseSlides.length) {
            currentScreenshotIndex = index;
            const imgElement = showcaseSlides[index];
            const imgSrc = imgElement.src;
            const imgAlt = imgElement.alt;
            const title = getScreenshotTitle(imgElement);

            // Update popup content
            screenshotPopupImage.src = imgSrc;
            screenshotPopupImage.alt = imgAlt;
            screenshotTitle.textContent = title;
            screenshotCounter.textContent = `${index + 1} of ${showcaseSlides.length}`;

            // Reset zoom and enable/disable navigation buttons
            resetZoom();
            screenshotPrevBtn.disabled = currentScreenshotIndex === 0;
            screenshotNextBtn.disabled = currentScreenshotIndex === showcaseSlides.length - 1;

            // Show popup
            screenshotPopup.classList.add('active');

            // Pause showcase carousel auto-scroll
            if (typeof window.showcaseAutoScrollInterval !== 'undefined') {
                clearInterval(window.showcaseAutoScrollInterval);
            }
        }
    }

    // Function to hide screenshot popup
    function hideScreenshotPopup() {
        screenshotPopup.classList.remove('active');

        // Resume showcase carousel auto-scroll
        if (showcaseSlides.length > 0) {
            const showcaseCarouselContainer = document.querySelector('.showcase-carousel');
            const showcasePrevBtn = document.querySelector('.showcase-prev-btn');
            const showcaseNextBtn = document.querySelector('.showcase-next-btn');

            window.showcaseAutoScrollInterval = setInterval(() => {
                showcaseNextBtn.click();
            }, 5000); // Restart auto-scroll
        }
    }

    // Add click event listeners to showcase images
    showcaseSlides.forEach((slide, index) => {
        slide.addEventListener('click', () => {
            showScreenshotPopup(index);
        });

        // Make cursor pointer to indicate clickable
        slide.style.cursor = 'pointer';
    });

    // Close popup event listeners
    closeScreenshotPopupBtn.addEventListener('click', hideScreenshotPopup);
    screenshotPopup.addEventListener('click', (e) => {
        if (e.target === screenshotPopup) {
            hideScreenshotPopup();
        }
    });

    // Zoom controls
    zoomInBtn.addEventListener('click', zoomIn);
    zoomOutBtn.addEventListener('click', zoomOut);
    zoomResetBtn.addEventListener('click', resetZoom);

    // Mouse wheel zoom
    screenshotPopupImage.addEventListener('wheel', (e) => {
        e.preventDefault();
        if (e.deltaY < 0) {
            zoomIn();
        } else {
            zoomOut();
        }
    });

    // Pan functionality (drag to move zoomed image)
    screenshotPopupImage.addEventListener('mousedown', (e) => {
        if (currentZoom > 1) {
            isPanning = true;
            startX = e.clientX;
            startY = e.clientY;
            const transform = getComputedStyle(screenshotPopupImage).transform;
            const matrix = new DOMMatrix(transform);
            initialX = matrix.m41 || 0;
            initialY = matrix.m42 || 0;
            screenshotPopupImage.style.cursor = 'grabbing';
        }
    });

    document.addEventListener('mousemove', (e) => {
        if (isPanning && currentZoom > 1) {
            const dx = e.clientX - startX;
            const dy = e.clientY - startY;
            const newX = initialX + dx;
            const newY = initialY + dy;
            screenshotPopupImage.style.transform = `scale(${currentZoom}) translate(${newX}px, ${newY}px)`;
        }
    });

    document.addEventListener('mouseup', () => {
        isPanning = false;
        screenshotPopupImage.style.cursor = currentZoom > 1 ? 'grab' : 'default';
    });

    // Double-click to reset zoom
    screenshotPopupImage.addEventListener('dblclick', resetZoom);

    // Navigation buttons event listeners
    screenshotPrevBtn.addEventListener('click', () => {
        if (currentScreenshotIndex > 0) {
            showScreenshotPopup(currentScreenshotIndex - 1);
        }
    });

    screenshotNextBtn.addEventListener('click', () => {
        if (currentScreenshotIndex < showcaseSlides.length - 1) {
            showScreenshotPopup(currentScreenshotIndex + 1);
        }
    });

    // Keyboard navigation
    document.addEventListener('keydown', (e) => {
        if (!screenshotPopup.classList.contains('active')) return;

        switch(e.key) {
            case 'ArrowLeft':
                if (e.ctrlKey || e.metaKey) {
                    zoomOut();
                } else {
                    screenshotPrevBtn.click();
                }
                break;
            case 'ArrowRight':
                if (e.ctrlKey || e.metaKey) {
                    zoomIn();
                } else {
                    screenshotNextBtn.click();
                }
                break;
            case 'ArrowUp':
                zoomIn();
                break;
            case 'ArrowDown':
                zoomOut();
                break;
            case '0':
            case 'Home':
                resetZoom();
                break;
            case 'Escape':
                hideScreenshotPopup();
                break;
        }
    });

    window.showScreenshotPopup = showScreenshotPopup; // Make it globally accessible
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

    // Function to apply theme
    function applyTheme(isDark) {
        if (isDark) {
            document.body.classList.add('dark-mode');
        } else {
            document.body.classList.remove('dark-mode');
        }
    }

    // Check for saved preference first
    const savedTheme = localStorage.getItem('darkMode');
    if (savedTheme === 'enabled') {
        applyTheme(true);
        darkModeSwitch.checked = true;
    } else if (savedTheme === 'disabled') {
        applyTheme(false);
        darkModeSwitch.checked = false;
    } else {
        // If no saved preference, check system preference
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        applyTheme(prefersDark);
        darkModeSwitch.checked = prefersDark;
    }

    // Listen for changes in system theme
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        // Only apply system theme if no explicit user preference is set
        if (localStorage.getItem('darkMode') === null) {
            applyTheme(e.matches);
            darkModeSwitch.checked = e.matches;
        }
    });

    // Dark mode toggle event listener
    darkModeSwitch.addEventListener('change', () => {
        if (darkModeSwitch.checked) {
            applyTheme(true);
            localStorage.setItem('darkMode', 'enabled');
        } else {
            applyTheme(false);
            localStorage.setItem('darkMode', 'disabled'); // Save 'disabled' to explicitly turn off dark mode
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
        setupScreenshotPopup(); // Initialize screenshot popup functionality for index.html
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
