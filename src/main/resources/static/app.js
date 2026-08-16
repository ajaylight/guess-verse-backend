const app = document.querySelector('#app');
const toastRegion = document.querySelector('#toast-region');

const categories = [
    ['general', 'General Knowledge', 'Light up the whole board', 'Sparkles', '#35d5e6', 38],
    ['science', 'Science', 'From atoms to astronomy', 'Atom', '#5ee3a1', 52],
    ['technology', 'Technology', 'Build your edge', 'Cpu', '#61a9ff', 24],
    ['movies', 'Movies & TV', 'Scenes worth knowing', 'Clapperboard', '#ffbf5e', 43],
    ['music', 'Music', 'Every era, every chorus', 'Music2', '#f36869', 31],
    ['sports', 'Sports', 'Play by the numbers', 'Trophy', '#f47b3b', 48],
    ['history', 'History', 'The stories that changed us', 'Landmark', '#bf96ff', 28],
    ['geography', 'Geography', 'A world of answers', 'Globe2', '#35d5e6', 36],
    ['gaming', 'Gaming', 'Know the next level', 'Gamepad2', '#5ee3a1', 19],
    ['anime', 'Anime', 'Icons, arcs, and legends', 'Ghost', '#ff7599', 26],
    ['programming', 'Programming', 'Syntax meets strategy', 'Code2', '#61a9ff', 33],
    ['mathematics', 'Mathematics', 'Patterns under pressure', 'Sigma', '#ffbf5e', 46]
];

const arenaModes = [
    ['Quick Battle', '10 logo challenges. Find the answer and keep your reward high.', 'Zap', 'cyan', 'Play Logo Arena'],
    ['Ranked Arena', 'Face equal-skill rivals and push your standing on the global board.', 'Medal', 'gold', 'Find a match'],
    ['Survival', 'Keep answering until the first mistake ends your run.', 'HeartPulse', 'orange', 'Start survival'],
    ['Time Attack', 'Build the biggest score before the clock runs out.', 'Timer', 'green', 'Race the clock'],
    ['Category Battle', 'Choose your specialist subject before matchmaking.', 'Swords', 'red', 'Choose category'],
    ['Private Arena', 'Bring your own crowd with a room code and custom rules.', 'LockKeyhole', 'cyan', 'Create a room']
];

const leaderboard = [
    ['1', 'NovaSage', 'NS', 'Level 42', '48,260', '278', '92%'],
    ['2', 'MiraMetric', 'MM', 'Level 39', '43,840', '254', '90%'],
    ['3', 'CircuitFinn', 'CF', 'Level 37', '40,180', '226', '89%'],
    ['4', 'AtlasV', 'AV', 'Level 35', '36,740', '195', '87%'],
    ['5', 'QuizKinetic', 'QK', 'Level 34', '34,920', '179', '86%'],
    ['6', 'LumaLogic', 'LL', 'Level 32', '31,580', '168', '85%']
];

const achievements = [
    ['First Blood', 'Win your first Arena match.', 'Swords', '100 XP', 100, true],
    ['Speed Demon', 'Answer 10 questions in under five seconds.', 'Gauge', '250 XP', 60, false],
    ['Perfect Mind', 'Score 100% in a quiz.', 'Crown', '500 XP', 0, false],
    ['Knowledge Hunter', 'Complete 50 quizzes.', 'Crosshair', '800 XP', 62, false],
    ['Arena Veteran', 'Play 100 matches.', 'Shield', '1,000 XP', 18, false],
    ['Unstoppable', 'Win 10 matches consecutively.', 'Flame', '1,200 XP', 40, false]
];

const tournaments = [
    ['Code Sprint Invitational', 'Programming', '1,200 players', '4,500 XP pool', 'Live', 'Live'],
    ['The Grand Cinema Quiz', 'Movies & TV', '840 players', '3,000 XP pool', 'Today, 19:30', 'Upcoming'],
    ['World Map Masters', 'Geography', '2,100 players', '8,000 XP pool', 'Completed', 'Completed']
];

const state = {
    token: localStorage.getItem('thinkarena.token') || '',
    user: readStore('thinkarena.user'),
    game: readStore('thinkarena.game'),
    result: readStore('thinkarena.result'),
    notificationsOpen: false,
    notificationsRead: false,
    settings: readStore('thinkarena.settings') || { sound: true, motion: true, reminders: true }
};

function readStore(key) {
    try { return JSON.parse(localStorage.getItem(key) || 'null'); } catch { return null; }
}

function saveStore(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
}

function icon(name, size = 18) {
    return `<i data-lucide="${name}" width="${size}" height="${size}" aria-hidden="true"></i>`;
}

function initials(value) {
    return (value || 'TA').split(/\s+/).map(part => part[0]).join('').slice(0, 2).toUpperCase();
}

function escapeHtml(value = '') {
    return String(value).replace(/[&<>'"]/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;' }[char]));
}

function currentPath() {
    return window.location.pathname.replace(/\/$/, '') || '/';
}

function navigate(path) {
    if (path === currentPath()) return;
    history.pushState({}, '', path);
    render();
}

async function api(path, options = {}) {
    const headers = { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) };
    if (state.token) headers.Authorization = `Bearer ${state.token}`;
    const response = await fetch(path, { ...options, headers });
    const contentType = response.headers.get('content-type') || '';
    const body = contentType.includes('application/json') ? await response.json() : null;
    if (!response.ok) throw new Error(body?.message || 'Unable to complete that action.');
    return body;
}

function toast(message, kind = 'info') {
    const node = document.createElement('div');
    node.className = `toast ${kind}`;
    node.innerHTML = `${icon(kind === 'error' ? 'CircleAlert' : kind === 'success' ? 'CircleCheck' : 'Info', 18)}<span>${escapeHtml(message)}</span>`;
    toastRegion.append(node);
    refreshIcons();
    window.setTimeout(() => node.remove(), 3600);
}

function refreshIcons() {
    if (window.lucide) window.lucide.createIcons({ attrs: { 'stroke-width': 1.8 } });
}

function brand() {
    return `<a class="brand" href="/" data-route aria-label="ThinkArena home"><span class="brand-mark">?</span><span class="brand-text">Think<span>Arena</span></span></a>`;
}

function navLink(path, label) {
    return `<a class="nav-link ${currentPath() === path ? 'is-active' : ''}" href="${path}" data-route>${label}</a>`;
}

function mobileLink(path, label, glyph) {
    const isActive = path === '/' ? currentPath() === '/' : currentPath().startsWith(path);
    return `<a class="mobile-link ${isActive ? 'is-active' : ''}" href="${path}" data-route>${icon(glyph, 19)}<span>${label}</span></a>`;
}

function header() {
    const user = state.user;
    return `<header class="topbar">
        ${brand()}
        <nav class="topnav" aria-label="Main navigation">
            ${navLink('/', 'Home')}${navLink('/arena', 'Arena')}${navLink('/categories', 'Categories')}${navLink('/leaderboard', 'Leaderboard')}${navLink('/tournaments', 'Tournaments')}
        </nav>
        <div class="header-actions">
            <button class="icon-button" type="button" data-action="search" aria-label="Search">${icon('Search')}</button>
            ${user ? `<button class="icon-button" type="button" data-action="notifications" aria-label="Notifications">${icon('Bell')}${state.notificationsRead ? '' : '<span class="unread-dot"></span>'}</button><div class="xp-chip">${icon('Sparkles', 15)} ${Number(user.xp || 0).toLocaleString()} XP</div><button class="profile-button" type="button" data-route="/profile/${encodeURIComponent(user.username)}" aria-label="Open profile"><span class="avatar">${initials(user.displayName || user.username)}</span></button>` : `<div class="auth-actions"><a class="button secondary small" href="/login" data-route>Login</a><a class="button small" href="/signup" data-route>Sign up</a></div>`}
        </div>
        ${state.notificationsOpen ? notificationMenu() : ''}
    </header>`;
}

function mobileNav() {
    return `<nav class="mobile-nav" aria-label="Mobile navigation">${mobileLink('/', 'Home', 'House')}${mobileLink('/arena', 'Arena', 'Swords')}${mobileLink('/leaderboard', 'Board', 'Trophy')}${mobileLink(state.user ? `/profile/${state.user.username}` : '/login', 'Profile', 'UserRound')}</nav>`;
}

function notificationMenu() {
    return `<div class="notification-menu" role="dialog" aria-label="Notifications">
        <div class="notification-head"><span>Notifications</span><button class="text-link" type="button" data-action="mark-read">Mark all read</button></div>
        <div class="notification">${icon('Trophy', 17)}<p><strong>Weekly board updated</strong><br>You are 14 places closer to Gold II.</p></div>
        <div class="notification">${icon('Flame', 17)}<p><strong>Keep the streak alive</strong><br>Complete today's challenge to reach 8 days.</p></div>
        <div class="notification">${icon('CalendarDays', 17)}<p><strong>World Map Masters</strong><br>Registration is open for the next round.</p></div>
    </div>`;
}

function shell(content) {
    return `<div class="app-shell">${header()}<main id="main-content">${content}</main>${mobileNav()}</div>`;
}

function dashboard() {
    const user = state.user;
    const greeting = user ? `Welcome back, ${escapeHtml(user.displayName || user.username)}` : 'Think fast. Play smart. Rule the Arena.';
    const copy = user ? 'Pick up your streak, take on a new challenge, or make a move on the weekly board.' : 'ThinkArena is the competitive knowledge platform for players who want every question to count.';
    return `<section class="page"><div class="hero">
        <div class="hero-copy"><div class="eyebrow">Live competitive knowledge</div><h1>${greeting}</h1><p>${copy}</p><div class="hero-actions"><button class="button gold" type="button" data-action="start-logo">${icon('Play', 17)} Enter the Arena</button><a class="button secondary" href="/categories" data-route>${icon('Grid2X2', 17)} Explore Categories</a></div><div class="hero-statline"><div class="hero-stat"><strong>12</strong><span>quiz categories</span></div><div class="hero-stat"><strong>24/7</strong><span>live arenas</span></div><div class="hero-stat"><strong>+250</strong><span>daily XP waiting</span></div></div></div>
        <div class="hero-art" aria-hidden="true"></div><div class="hero-live"><span class="pulse"></span> 1,248 players in arena</div>
    </div>
    <div class="dashboard-grid"><div><div class="section-heading"><div><h2>Live Arena</h2><p>Competitive sessions opening now.</p></div><a class="text-link" href="/arena" data-route>View all arenas</a></div><div class="card-grid">${liveArenaCard('Science Blitz', 'Atom', '12 Players', 'Medium', 'Free', 'Starts in 02:14')}${liveArenaCard('Pixel Perfect', 'Gamepad2', '8 Players', 'Hard', '50 XP', 'Starts in 01:38')}${liveArenaCard('World Map Rush', 'Globe2', '16 Players', 'Medium', 'Free', 'Starts in 03:02')}</div></div>${dailyPanel()}</div>
    <div class="section-heading"><div><h2>Find your category</h2><p>Build a specialty or mix it up.</p></div><a class="text-link" href="/categories" data-route>All categories</a></div><div class="category-grid">${categories.slice(0, 8).map(categoryCard).join('')}</div></section>`;
}

function liveArenaCard(name, glyph, players, difficulty, entry, starts) {
    return `<article class="card live-card"><div class="live-card-top"><span class="live-icon">${icon(glyph, 20)}</span><div><h3>${name}</h3><span class="muted mono" style="font-size:11px">Matchmaking open</span></div></div><div class="tags"><span class="tag green">${players}</span><span class="tag">${difficulty}</span><span class="tag ${entry === 'Free' ? '' : 'hot'}">${entry}</span></div><div class="live-meta"><span>${starts}</span><button class="button small" type="button" data-action="start-logo">Join arena</button></div></article>`;
}

function dailyPanel() {
    const played = Boolean(state.result?.completed);
    return `<aside><div class="daily-card"><div class="eyebrow" style="color:var(--gold)">Today's mission</div><h2>Daily Challenge</h2><div class="daily-points">+250 XP</div><div class="daily-kpis"><div class="daily-kpi"><span>Questions</span><strong>10</strong></div><div class="daily-kpi"><span>Difficulty</span><strong>Medium</strong></div><div class="daily-kpi"><span>Status</span><strong>${played ? 'Complete' : 'Ready'}</strong></div></div><div class="streak">${icon('Flame', 17)} 7 Day Streak</div><div class="streak-week"><span class="day done">M</span><span class="day done">T</span><span class="day done">W</span><span class="day done">T</span><span class="day done">F</span><span class="day done">S</span><span class="day">S</span></div><button class="button gold full" style="margin-top:20px" type="button" data-action="start-logo">${played ? 'Play again' : 'Start challenge'}</button></div><div class="panel rank-card" style="margin-top:18px"><span class="eyebrow">Current rank</span><div class="rank-name"><span class="rank-badge">II</span> Gold II</div><p class="muted" style="font-size:13px">160 rating to Gold I</p><div class="progress"><span style="width:72%"></span></div><div class="streak-week"><span class="day done">3</span><span class="day done">7</span><span class="day">14</span><span class="day">30</span></div></div></aside>`;
}

function categoryCard(category) {
    const [id, title, subtitle, glyph, color, progress] = category;
    return `<a class="card category-card" href="/category/${id}" data-route style="--category-color:${color}"><span class="category-icon">${icon(glyph, 20)}</span><h3>${title}</h3><p>${subtitle}</p><div class="micro-progress"><label><span>${progress}% explored</span><span>${Math.round(progress / 4)} quizzes</span></label><div class="progress"><span style="width:${progress}%;background:${color}"></span></div></div></a>`;
}

function arenaPage() {
    return `<section class="page"><div class="page-header"><div><div class="eyebrow">Find your format</div><h1>Choose Your Arena</h1><p>Every mode rewards a different kind of thinking. Start with Logo Arena, then take your score to the next battle.</p></div></div><div class="arena-grid">${arenaModes.map((mode, index) => `<article class="card arena-card ${mode[3]}"><span class="arena-icon">${icon(mode[2], 22)}</span><h3>${mode[0]}</h3><p>${mode[1]}</p><button class="button small ${index === 0 ? '' : 'secondary'}" type="button" data-action="${index === 0 ? 'start-logo' : 'unavailable'}">${mode[4]} ${icon('ArrowUpRight', 15)}</button></article>`).join('')}</div><div class="section-heading"><div><h2>How Logo Arena works</h2><p>One game, ten logo questions, no answer is sent to the browser before you make your call.</p></div></div><div class="card-grid"><article class="panel"><span class="eyebrow">01</span><h3>Read the visual</h3><p class="muted">Use the image and 14-letter bank to find the brand.</p></article><article class="panel"><span class="eyebrow">02</span><h3>Protect the reward</h3><p class="muted">Info and reveal hints reduce the current question value.</p></article><article class="panel"><span class="eyebrow">03</span><h3>Finish strong</h3><p class="muted">Complete all ten questions to lock in your result.</p></article></div></section>`;
}

function categoriesPage() {
    return `<section class="page"><div class="page-header"><div><div class="eyebrow">Knowledge library</div><h1>Choose a category</h1><p>Specialize for precision or keep your game broad. Every category has curated, competitive-ready quizzes.</p></div><div class="segmented"><button class="segment active" type="button">Popular</button><button class="segment" type="button">New</button><button class="segment" type="button">For you</button></div></div><div class="category-grid">${categories.map(categoryCard).join('')}</div></section>`;
}

function categoryPage(id) {
    const category = categories.find(item => item[0] === id) || categories[0];
    const [categoryId, title, subtitle, glyph, color] = category;
    const titles = [`${title}: opening moves`, `${title} under pressure`, `The ${title} deep cut`, `${title} speed round`];
    return `<section class="page"><div class="card category-banner"><span class="category-icon" style="color:${color}">${icon(glyph, 30)}</span><div><div class="eyebrow">Category arena</div><h1 style="font-size:34px;margin:0 0 7px">${title}</h1><p class="muted" style="margin:0">${subtitle}. Challenge yourself across quick play, precision rounds, and hard mode.</p></div><div class="banner-stats"><div class="banner-stat"><strong>#182</strong><span>Your rank</span></div><div class="banner-stat"><strong>1,240</strong><span>Category XP</span></div><div class="banner-stat"><strong>84%</strong><span>Accuracy</span></div></div></div><div class="section-heading"><div><h2>Quiz selection</h2><p>Choose a format or go straight into a verified Logo Arena game.</p></div><div class="segmented"><button class="segment active" type="button">Popular</button><button class="segment" type="button">New</button><button class="segment" type="button">Quick play</button><button class="segment" type="button">Hard mode</button></div></div><div class="quiz-list">${titles.map((quiz, index) => `<article class="card quiz-card"><div><div class="eyebrow">${index % 2 ? 'Hard mode' : 'Quick play'}</div><h3>${quiz}</h3><p>A focused ${title.toLowerCase()} set with sharp explanations and a score that rewards confidence.</p><div class="quiz-stats"><span>10 questions</span><span>${index + 3} min</span><span>${index % 2 ? 'Hard' : 'Medium'}</span><span>+${250 + index * 50} XP</span></div></div><button class="button small" type="button" data-action="start-logo">Play ${icon('Play', 15)}</button></article>`).join('')}</div></section>`;
}

function gamePage() {
    const game = state.game;
    if (!game?.id || !game?.question) return emptyPage('Quiz unavailable', 'Start a new Logo Arena session to enter the game.', 'Start Logo Arena', 'start-logo');
    if (game.review) return gameReviewPage(game);
    const question = game.question;
    const total = Number(game.totalQuestions || 10);
    const number = Number(question.questionNumber || game.questionNumber || 1);
    const answerLength = Number(question.answerLength || 0);
    const answer = game.answer || [];
    const revealed = game.revealed || {};
    const slots = Array.from({ length: answerLength }, (_, position) => {
        const revealedLetter = revealed[position];
        const selected = answer[position];
        return `<button class="answer-slot ${revealedLetter ? 'revealed' : selected ? 'filled' : ''}" type="button" data-action="remove-letter" data-position="${position}" aria-label="Answer position ${position + 1}">${escapeHtml(revealedLetter || selected?.letter || '')}</button>`;
    }).join('');
    const letters = (question.letters || []).map((letter, index) => `<button class="letter ${answer.some(item => item.index === index) ? 'used' : ''}" type="button" data-action="pick-letter" data-index="${index}" data-letter="${escapeHtml(letter)}" aria-label="Choose letter ${escapeHtml(letter)}">${escapeHtml(letter)}</button>`).join('');
    return `<section class="game-page"><div class="game-top">${brand()}<div class="game-counter">Question ${number} / ${total}</div><div class="game-score">${Number(game.score || 0).toLocaleString()} XP</div><div class="game-progress progress"><span style="width:${(number / total) * 100}%"></span></div></div><div class="question-layout"><article class="panel question-panel"><p class="question-title">Identify the logo</p><div class="logo-stage">${question.imageName ? `<img src="${escapeHtml(question.imageName)}" data-logo alt="Logo challenge" loading="eager"><div class="logo-fallback" style="display:none">TA</div>` : '<div class="logo-fallback">TA</div>'}</div><div class="answer-slots">${slots}</div><div class="letter-bank">${letters}</div><div class="game-actions"><button class="button secondary" type="button" data-action="clear-answer">Clear</button><button class="button" type="button" data-action="submit-guess" ${answer.filter(Boolean).length !== answerLength ? 'disabled' : ''}>Submit answer ${icon('ArrowRight', 16)}</button></div><div class="game-feedback ${game.feedback?.kind || ''}">${escapeHtml(game.feedback?.text || '')}</div></article><aside class="game-sidebar"><div class="panel side-card"><h3>Current reward</h3><div class="reward-value">${Number(game.reward || 100)} XP</div><p class="muted" style="font-size:12px;margin:8px 0 0">Hints reduce this round's value.</p></div><div class="panel side-card"><h3>Power-ups</h3><div class="power-list"><button class="power" type="button" data-action="hint-letter" title="Reveal the next unrevealed answer position">${icon('ScanLine', 18)}<span>Reveal letter<br><small>-10 XP minimum</small></span></button><button class="power" type="button" data-action="hint-info" title="Show a clue about this logo">${icon('Lightbulb', 18)}<span>Get info<br><small>-30 XP</small></span></button><button class="power" type="button" data-action="hint-answer" title="Reveal the answer and continue for 20 XP">${icon('Eye', 18)}<span>Reveal answer<br><small>20 XP reward</small></span></button><button class="power" type="button" disabled title="Time Attack is not enabled for Logo Arena yet">${icon('Snowflake', 18)}<span>Freeze time<br><small>Mode specific</small></span></button></div>${game.info ? `<div class="hint-info">${escapeHtml(game.info)}</div>` : ''}</div></aside></div></section>`;
}

function gameReviewPage(game) {
    const reveal = game.review;
    return `<section class="game-page"><div class="game-top">${brand()}<div class="game-counter">Question ${reveal.questionNumber} / ${game.totalQuestions || 10}</div><div class="game-score">${Number(reveal.totalScore || game.score || 0).toLocaleString()} XP</div><div class="game-progress progress"><span style="width:${(Number(reveal.questionNumber || 1) / Number(game.totalQuestions || 10)) * 100}%"></span></div></div><article class="panel reveal-panel"><div class="reveal-kicker">${reveal.correct ? 'Correct answer' : 'Answer revealed'}</div><div class="reveal-answer">${escapeHtml(reveal.answer)}</div><div class="reveal-points">+${reveal.questionReward} XP &nbsp; | &nbsp; ${Number(reveal.totalScore).toLocaleString()} total</div><p class="reveal-info">${escapeHtml(reveal.info || 'Nice work. Keep moving to maximize your final score.')}</p><button class="button ${reveal.gameCompleted ? 'gold' : ''}" type="button" data-action="${reveal.gameCompleted ? 'view-results' : 'continue-game'}">${reveal.gameCompleted ? 'View results' : `Next question ${icon('ArrowRight', 16)}`}</button></article></section>`;
}

function resultsPage() {
    const result = state.result;
    if (!result) return emptyPage('No result yet', 'Finish a Logo Arena game to see its final score.', 'Enter the Arena', 'start-logo');
    const accuracy = Number(result.accuracy || 0).toFixed(0);
    return `<section class="game-page"><article class="panel reveal-panel"><div class="eyebrow">Arena complete</div><h1 style="font-size:42px;margin-bottom:8px">Your result</h1><p class="muted">A complete Logo Arena session is saved server-side. Here is how the run landed.</p><div class="reveal-answer">${Number(result.totalScore || 0).toLocaleString()} XP</div><div class="reveal-points">${result.correctAnswers} / ${result.totalQuestions} correct &nbsp; | &nbsp; ${accuracy}% accuracy</div><div class="card-grid" style="text-align:left;margin:26px 0"><div class="card"><span class="muted">Hints used</span><h2>${result.hintsUsed}</h2></div><div class="card"><span class="muted">Letters revealed</span><h2>${result.lettersRevealed}</h2></div><div class="card"><span class="muted">Maximum possible</span><h2>${Number(result.maximumScore).toLocaleString()}</h2></div></div><div class="hero-actions" style="justify-content:center"><button class="button gold" type="button" data-action="start-logo">${icon('RotateCcw', 16)} Play again</button><a class="button secondary" href="/arena" data-route>Back to Arena</a><button class="button secondary" type="button" data-action="share-result">${icon('Share2', 16)} Share result</button></div></article></section>`;
}

function leaderboardPage() {
    const current = state.user ? ['247', state.user.displayName || state.user.username, initials(state.user.displayName || state.user.username), `Level ${state.user.level || 1}`, Number(state.user.xp || 0).toLocaleString(), '0', '-'] : null;
    return `<section class="page"><div class="page-header"><div><div class="eyebrow">The competitive board</div><h1>Leaderboard</h1><p>Every correct answer is a move. Review the board and set your next target.</p></div><div class="segmented"><button class="segment active" type="button">Global</button><button class="segment" type="button">Friends</button><button class="segment" type="button">Weekly</button><button class="segment" type="button">Monthly</button></div></div><div class="leaderboard-layout"><div><div class="podium">${leaderboard.slice(0,3).map(row => `<article class="card podium-card"><div class="podium-rank">#${row[0]}</div><span class="avatar">${row[2]}</span><h3>${row[1]}</h3><p class="muted mono" style="font-size:11px;margin:0">${row[4]} XP</p></article>`).join('')}</div><div class="table">${[...leaderboard, ...(current ? [current] : [])].map(row => `<div class="leader-row ${current && row[0] === '247' ? 'me' : ''}"><strong class="mono">#${row[0]}</strong><div class="leader-user"><span class="avatar">${row[2]}</span><span>${escapeHtml(row[1])}<small class="leader-muted">${row[3]}</small></span></div><span class="leader-muted">${row[4]} XP</span><span class="leader-muted">${row[5]} wins</span><span class="leader-muted">${row[6]}</span></div>`).join('')}</div></div><aside class="panel sticky-rank"><div class="eyebrow">Your position</div><h2>${current ? '#247' : '#247'}</h2><p class="muted">${current ? escapeHtml(current[1]) : 'AJ'}, you are on the board.</p><div class="stat-list"><div class="stat-line"><span>Rating</span><strong>1,840</strong></div><div class="stat-line"><span>Next rank</span><strong>Gold I</strong></div><div class="stat-line"><span>Remaining</span><strong>160</strong></div></div><div class="progress" style="margin-top:18px"><span style="width:72%"></span></div><a class="button full secondary" href="/arena" data-route style="margin-top:18px">Move up</a></aside></div></section>`;
}

function achievementsPage() {
    return `<section class="page"><div class="page-header"><div><div class="eyebrow">Proof of play</div><h1>Achievements</h1><p>Every milestone is a marker of how you play, not just how much you know.</p></div><span class="tag green">1 / 6 unlocked</span></div><div class="achievement-grid">${achievements.map(item => `<article class="card achievement ${item[5] ? '' : 'locked'}"><div class="achievement-top"><span class="achievement-icon">${icon(item[2], 20)}</span><span class="tag">${item[3]}</span></div><h3 style="margin-top:18px">${item[0]}</h3><p>${item[1]}</p><div class="achievement-footer"><span>${item[5] ? 'Unlocked today' : `${item[4]}% complete`}</span><span>${item[5] ? icon('Check', 15) : ''}</span></div>${item[5] ? '' : `<div class="progress" style="margin-top:9px"><span style="width:${item[4]}%"></span></div>`}</article>`).join('')}</div></section>`;
}

function tournamentsPage() {
    return `<section class="page"><div class="page-header"><div><div class="eyebrow">Compete for more</div><h1>Tournaments</h1><p>Timed brackets, high-stakes categories, and XP pools built for the competitive mind.</p></div><div class="segmented"><button class="segment active" type="button">All</button><button class="segment" type="button">Live</button><button class="segment" type="button">Upcoming</button><button class="segment" type="button">Completed</button></div></div><div class="tournament-grid">${tournaments.map(item => `<article class="card tournament-card"><span class="tournament-status ${item[5].toLowerCase()}">${item[5]}</span><h3 style="margin-top:18px">${item[0]}</h3><p>${item[1]} tournament with high-pressure rounds and an open competitive field.</p><div class="tournament-meta"><span>${item[2]}</span><span>${item[3]}</span><span>${item[4]}</span></div><button class="button small ${item[5] === 'Completed' ? 'secondary' : ''}" type="button" data-action="unavailable">${item[5] === 'Live' ? 'Join tournament' : item[5] === 'Upcoming' ? 'Register' : 'View results'}</button></article>`).join('')}</div></section>`;
}

function profilePage() {
    const user = state.user || { username: 'arena-player', displayName: 'Arena Player', level: 18, xp: 14850, coins: 0 };
    const name = user.displayName || user.username;
    return `<section class="page"><div class="panel profile-hero"><span class="avatar profile-avatar">${initials(name)}</span><div><div class="eyebrow">Player profile</div><h1>${escapeHtml(name)}</h1><p>@${escapeHtml(user.username)} &nbsp; | &nbsp; India &nbsp; | &nbsp; Joined this season</p></div><div class="profile-level"><strong>Gold II</strong><span class="muted">${Number(user.xp || 0).toLocaleString()} XP</span></div></div><div class="profile-stats">${[['Matches played', '48'], ['Wins', '29'], ['Win rate', '60%'], ['Questions', '482'], ['Accuracy', '84%'], ['Best streak', '11']].map(stat => `<div class="profile-stat"><strong>${stat[1]}</strong><span>${stat[0]}</span></div>`).join('')}</div><div class="dashboard-grid" style="margin-top:18px"><article class="panel"><div class="section-heading" style="margin:0 0 17px"><div><h2>Rank progression</h2><p>160 rating to Gold I.</p></div></div><div class="progress"><span style="width:72%"></span></div><div class="section-heading"><div><h2>Recent activity</h2><p>Logo Arena is connected to your session history.</p></div></div><div class="table"><div class="leader-row"><span class="mono">Today</span><div class="leader-user">${icon('Sparkles', 17)}<span>Logo Arena</span></div><span class="leader-muted">+${state.result?.totalScore || 0} XP</span><span class="leader-muted">${state.result?.completed ? 'Completed' : 'Ready'}</span></div><div class="leader-row"><span class="mono">Yesterday</span><div class="leader-user">${icon('Trophy', 17)}<span>Science Blitz</span></div><span class="leader-muted">+420 XP</span><span class="leader-muted">Win</span></div></div></article><aside class="panel"><div class="eyebrow">Favorite categories</div>${categories.slice(0,4).map(cat => `<div class="setting-row"><span style="color:${cat[4]}">${icon(cat[3], 17)}</span><strong>${cat[1]}</strong><span>${cat[5]}%</span></div>`).join('')}<a class="button full secondary" href="/settings" data-route style="margin-top:16px">Edit profile</a></aside></div></section>`;
}

function settingsPage() {
    return `<section class="page"><div class="page-header"><div><div class="eyebrow">Player controls</div><h1>Settings</h1><p>Set up the arena so it works the way you play.</p></div></div><div class="settings-grid"><aside class="panel side-menu"><button class="active" type="button">Account</button><button type="button">Profile</button><button type="button">Notifications</button><button type="button">Gameplay</button><button type="button">Appearance</button></aside><article class="panel"><h2>Gameplay preferences</h2><p class="muted">These controls are stored on this device until profile preferences are available through the API.</p>${settingRow('Sound effects', 'Play confirmation feedback during answers.', 'sound')}${settingRow('Reduced motion', 'Keep score and answer feedback calm.', 'motion')}${settingRow('Daily reminder', 'Remind me before the challenge resets.', 'reminders')}<div class="section-heading"><div><h2>Account</h2><p>Your authenticated profile is served by GuessVerse.</p></div></div>${state.user ? `<div class="setting-row"><div><strong>${escapeHtml(state.user.email || '')}</strong><span>Signed in as ${escapeHtml(state.user.username)}</span></div><button class="button danger small" type="button" data-action="logout">Log out</button></div>` : `<a class="button" href="/login" data-route>Log in</a>`}</article></div></section>`;
}

function settingRow(title, detail, key) {
    return `<div class="setting-row"><div><strong>${title}</strong><span>${detail}</span></div><button class="switch ${state.settings[key] ? 'on' : ''}" type="button" data-action="toggle-setting" data-setting="${key}" aria-pressed="${state.settings[key]}" aria-label="Toggle ${title}"></button></div>`;
}

function searchPage() {
    return `<section class="page"><div class="search-panel"><div class="eyebrow">Discover the arena</div><h1 style="font-size:42px">Search ThinkArena</h1><div class="panel"><label class="search-input">${icon('Search', 20)}<input id="search-input" type="search" placeholder="Quizzes, categories, players, tournaments" autocomplete="off"></label><div id="search-results" class="search-results"><div class="empty-state">${icon('SearchX', 28)}<h3>Search the arena</h3><p>Start typing to explore categories, games, players, and tournaments.</p></div></div></div></div></section>`;
}

function authPage(mode) {
    const signup = mode === 'signup';
    return `<section class="auth-page"><aside class="auth-aside"><div class="auth-aside-content">${brand()}<div class="eyebrow" style="margin-top:72px">Competitive knowledge begins here</div><h1>${signup ? 'Build a profile worth playing for.' : 'Every answer moves you forward.'}</h1><p>${signup ? 'Choose the identity that will appear on the board. Your first arena is ready after registration.' : 'Sign in to continue your run, track your XP, and meet the next challenge.'}</p></div></aside><div class="auth-form-wrap"><form class="auth-form" id="${signup ? 'signup' : 'login'}-form"><div class="eyebrow">${signup ? 'New player' : 'Welcome back'}</div><h2>${signup ? 'Create your account' : 'Log in to ThinkArena'}</h2><p class="muted">${signup ? 'Your profile starts at Level 1 with a fresh streak.' : 'Use your GuessVerse account to continue.'}</p>${signup ? `<div class="field"><label for="display-name">Display name</label><input id="display-name" name="displayName" required maxlength="50" placeholder="Arena name"></div><div class="field"><label for="username">Username</label><input id="username" name="username" required maxlength="30" pattern="[A-Za-z0-9_]+" placeholder="your_handle"></div>` : ''}<div class="field"><label for="email">Email</label><input id="email" name="email" type="email" required placeholder="you@example.com"></div><div class="field"><label for="password">Password</label><input id="password" name="password" type="password" required minlength="6" placeholder="Enter your password"></div>${signup ? `<div class="field"><label for="confirm-password">Confirm password</label><input id="confirm-password" name="confirmPassword" type="password" required minlength="6" placeholder="Repeat your password"></div>` : ''}<p id="form-error" class="form-error" role="alert"></p><button class="button full" type="submit">${signup ? 'Create account' : 'Log in'} ${icon('ArrowRight', 17)}</button><p class="auth-alt">${signup ? 'Already on the board?' : 'New to ThinkArena?'} <a href="/${signup ? 'login' : 'signup'}" data-route>${signup ? 'Log in' : 'Create an account'}</a></p>${signup ? '' : '<p class="auth-alt"><a href="/forgot-password" data-route>Forgot password?</a></p>'}</form></div></section>`;
}

function forgotPasswordPage() {
    return `<section class="auth-page"><aside class="auth-aside"><div class="auth-aside-content">${brand()}<div class="eyebrow" style="margin-top:72px">Account recovery</div><h1>Get back into the arena.</h1><p>Password recovery needs a mail-delivery endpoint before it can send messages. Use your existing credentials to log in for now.</p></div></aside><div class="auth-form-wrap"><div class="auth-form"><div class="eyebrow">Coming next</div><h2>Reset password</h2><p class="muted">This backend has not exposed password reset yet.</p><a class="button full" href="/login" data-route>Back to login</a></div></div></section>`;
}

function emptyPage(title, description, actionLabel, action) {
    return `<section class="page"><div class="panel empty-state">${icon('CircleDashed', 34)}<h1 style="font-size:38px">${title}</h1><p>${description}</p><button class="button" type="button" data-action="${action}">${actionLabel}</button></div></section>`;
}

function friendsPage() { return emptyPage('Your squad is waiting', 'Friend lists and challenges need the corresponding multiplayer endpoints. The Arena is ready when your squad is.', 'Enter the Arena', 'start-logo'); }
function roomPage() { return emptyPage('Private rooms are almost ready', 'Room creation already exists in the backend but has not yet been connected to the ThinkArena flow.', 'Explore Arena', 'navigate-arena'); }

function renderPage(path) {
    if (path === '/') return dashboard();
    if (path === '/arena') return arenaPage();
    if (path === '/categories') return categoriesPage();
    if (path.startsWith('/category/')) return categoryPage(path.split('/').pop());
    if (path.startsWith('/play/')) return gamePage();
    if (path.startsWith('/results/')) return resultsPage();
    if (path === '/leaderboard') return leaderboardPage();
    if (path === '/achievements') return achievementsPage();
    if (path === '/tournaments' || path.startsWith('/tournament/')) return tournamentsPage();
    if (path === '/profile' || path.startsWith('/profile/')) return profilePage();
    if (path === '/settings') return settingsPage();
    if (path === '/search') return searchPage();
    if (path === '/friends') return friendsPage();
    if (path === '/join' || path.startsWith('/room/')) return roomPage();
    if (path === '/login') return authPage('login');
    if (path === '/signup') return authPage('signup');
    if (path === '/forgot-password') return forgotPasswordPage();
    return emptyPage('This arena does not exist', 'Choose a destination from the main navigation.', 'Go home', 'navigate-home');
}

function render() {
    app.innerHTML = shell(renderPage(currentPath()));
    refreshIcons();
}

async function startLogoArena() {
    try {
        toast('Creating your Logo Arena session...');
        const data = await api('/api/logo/start');
        state.game = { id: data.gameId, question: data.question, score: 0, reward: 100, questionNumber: 1, totalQuestions: 10, answer: [], revealed: {}, info: '', feedback: null, review: null };
        saveStore('thinkarena.game', state.game);
        navigate(`/play/${data.gameId}`);
    } catch (error) {
        toast(error.message, 'error');
    }
}

function updateGame(next) {
    state.game = { ...state.game, ...next };
    saveStore('thinkarena.game', state.game);
}

function pickLetter(index, letter) {
    const game = state.game;
    const slots = Number(game.question.answerLength || 0);
    const answer = [...(game.answer || [])];
    const position = Array.from({ length: slots }, (_, i) => i).find(i => !game.revealed?.[i] && !answer[i]);
    if (position === undefined) return;
    answer[position] = { index: Number(index), letter };
    updateGame({ answer, feedback: null });
    render();
}

function removeLetter(position) {
    const game = state.game;
    if (game.revealed?.[position]) return;
    const answer = [...(game.answer || [])];
    delete answer[position];
    updateGame({ answer, feedback: null });
    render();
}

function clearAnswer() {
    updateGame({ answer: [], feedback: null });
    render();
}

function answerValue() {
    const game = state.game;
    return (game.answer || []).map((item, index) => game.revealed?.[index] || item?.letter || '').join('');
}

async function submitGuess() {
    const game = state.game;
    if (!game) return;
    try {
        const data = await api(`/api/logo/${game.id}/guess`, { method: 'POST', body: JSON.stringify({ guess: answerValue() }) });
        if (!data.correct) {
            updateGame({ feedback: { kind: 'wrong', text: data.message || 'Wrong answer. Try another combination.' } });
            render();
            return;
        }
        updateGame({ score: data.score, review: data.reveal, feedback: null });
        render();
        toast(`Correct. +${data.reveal.questionReward} XP`, 'success');
    } catch (error) {
        toast(error.message, 'error');
    }
}

async function useInfoHint() {
    try {
        const data = await api(`/api/logo/${state.game.id}/hint/info`, { method: 'POST' });
        updateGame({ reward: data.score, info: data.info || state.game.info });
        render();
    } catch (error) { toast(error.message, 'error'); }
}

async function useLetterHint() {
    const length = Number(state.game.question.answerLength || 0);
    const position = Array.from({ length }, (_, index) => index).find(index => !state.game.revealed?.[index]);
    if (position === undefined) return toast('Every letter is already revealed.');
    try {
        const data = await api(`/api/logo/${state.game.id}/hint/letter`, { method: 'POST', body: JSON.stringify({ position }) });
        updateGame({ reward: data.score, revealed: { ...(state.game.revealed || {}), [data.revealedPosition]: data.revealedLetter }, feedback: null });
        render();
    } catch (error) { toast(error.message, 'error'); }
}

async function useAnswerHint() {
    try {
        const data = await api(`/api/logo/${state.game.id}/hint/answer`, { method: 'POST' });
        updateGame({ score: state.game.score + 20, review: data.reveal, feedback: null });
        render();
    } catch (error) { toast(error.message, 'error'); }
}

async function continueGame() {
    try {
        const question = await api(`/api/logo/${state.game.id}/continue`, { method: 'POST' });
        updateGame({ question, score: question.score, reward: 100, questionNumber: question.questionNumber, answer: [], revealed: {}, info: '', feedback: null, review: null });
        render();
    } catch (error) { toast(error.message, 'error'); }
}

async function loadResults() {
    try {
        const result = await api(`/api/logo/${state.game.id}/result`);
        state.result = result;
        saveStore('thinkarena.result', result);
        navigate(`/results/${state.game.id}`);
    } catch (error) { toast(error.message, 'error'); }
}

async function authenticate(event, mode) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = Object.fromEntries(new FormData(form));
    const errorNode = form.querySelector('#form-error');
    errorNode.textContent = '';
    if (mode === 'signup' && data.password !== data.confirmPassword) {
        errorNode.textContent = 'Passwords do not match.';
        return;
    }
    const payload = mode === 'signup' ? { username: data.username, displayName: data.displayName, email: data.email, password: data.password } : { email: data.email, password: data.password };
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    try {
        const response = await api(`/api/auth/${mode === 'signup' ? 'register' : 'login'}`, { method: 'POST', body: JSON.stringify(payload) });
        state.token = response.token;
        localStorage.setItem('thinkarena.token', state.token);
        await loadUser();
        toast(mode === 'signup' ? 'Account created. Your first Arena is ready.' : 'Welcome back to the Arena.', 'success');
        navigate('/');
    } catch (error) {
        errorNode.textContent = error.message;
    } finally {
        button.disabled = false;
    }
}

async function loadUser() {
    if (!state.token) return;
    try {
        state.user = await api('/api/users/me');
        saveStore('thinkarena.user', state.user);
    } catch {
        state.token = '';
        state.user = null;
        localStorage.removeItem('thinkarena.token');
        localStorage.removeItem('thinkarena.user');
    }
}

function logout() {
    state.token = '';
    state.user = null;
    localStorage.removeItem('thinkarena.token');
    localStorage.removeItem('thinkarena.user');
    toast('You have been logged out.');
    navigate('/');
}

function renderSearch(value) {
    const target = document.querySelector('#search-results');
    if (!target) return;
    const term = value.trim().toLowerCase();
    if (!term) {
        target.innerHTML = `<div class="empty-state">${icon('SearchX', 28)}<h3>Search the arena</h3><p>Start typing to explore categories, games, players, and tournaments.</p></div>`;
        refreshIcons();
        return;
    }
    const matches = [
        ...categories.map(category => ({ title: category[1], type: 'Category', route: `/category/${category[0]}`, icon: category[3] })),
        ...tournaments.map(item => ({ title: item[0], type: 'Tournament', route: '/tournaments', icon: 'Trophy' })),
        ...leaderboard.map(item => ({ title: item[1], type: 'Player', route: `/profile/${item[1]}`, icon: 'UserRound' }))
    ].filter(item => item.title.toLowerCase().includes(term)).slice(0, 8);
    target.innerHTML = matches.length ? `<div class="panel">${matches.map(item => `<a class="search-result" href="${item.route}" data-route><span>${icon(item.icon, 18)} <strong>${item.title}</strong></span><small>${item.type} ${icon('ArrowUpRight', 14)}</small></a>`).join('')}</div>` : `<div class="empty-state">${icon('SearchX', 28)}<h3>No exact match</h3><p>Try a category, player, or tournament name.</p></div>`;
    refreshIcons();
}

document.addEventListener('click', event => {
    const route = event.target.closest('[data-route]');
    if (route && route.tagName === 'A') {
        event.preventDefault();
        navigate(route.getAttribute('href'));
        return;
    }
    if (route && route.tagName === 'BUTTON') {
        navigate(route.dataset.route);
        return;
    }
    const actionNode = event.target.closest('[data-action]');
    if (!actionNode || actionNode.disabled) return;
    const action = actionNode.dataset.action;
    if (action === 'start-logo') startLogoArena();
    if (action === 'pick-letter') pickLetter(actionNode.dataset.index, actionNode.dataset.letter);
    if (action === 'remove-letter') removeLetter(Number(actionNode.dataset.position));
    if (action === 'clear-answer') clearAnswer();
    if (action === 'submit-guess') submitGuess();
    if (action === 'hint-info') useInfoHint();
    if (action === 'hint-letter') useLetterHint();
    if (action === 'hint-answer') useAnswerHint();
    if (action === 'continue-game') continueGame();
    if (action === 'view-results') loadResults();
    if (action === 'notifications') { state.notificationsOpen = !state.notificationsOpen; render(); }
    if (action === 'mark-read') { state.notificationsRead = true; state.notificationsOpen = false; render(); }
    if (action === 'search') navigate('/search');
    if (action === 'logout') logout();
    if (action === 'toggle-setting') { state.settings[actionNode.dataset.setting] = !state.settings[actionNode.dataset.setting]; saveStore('thinkarena.settings', state.settings); render(); }
    if (action === 'share-result') { navigator.clipboard?.writeText(`I scored ${state.result?.totalScore || 0} XP in ThinkArena Logo Arena.`).then(() => toast('Result copied to clipboard.', 'success')).catch(() => toast('Share text is ready to copy.')); }
    if (action === 'navigate-home') navigate('/');
    if (action === 'navigate-arena') navigate('/arena');
    if (action === 'unavailable') toast('This arena needs its matching backend endpoint before it can open.', 'info');
});

document.addEventListener('submit', event => {
    if (event.target.id === 'login-form') authenticate(event, 'login');
    if (event.target.id === 'signup-form') authenticate(event, 'signup');
});

document.addEventListener('input', event => {
    if (event.target.id === 'search-input') renderSearch(event.target.value);
});

document.addEventListener('error', event => {
    const image = event.target;
    if (image.matches?.('img[data-logo]')) {
        image.style.display = 'none';
        const fallback = image.nextElementSibling;
        if (fallback) fallback.style.display = 'grid';
    }
}, true);

window.addEventListener('popstate', render);

async function bootstrap() {
    await loadUser();
    render();
}

bootstrap();
