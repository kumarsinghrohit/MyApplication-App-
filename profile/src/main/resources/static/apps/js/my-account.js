const DrawerState = {
  OPEN: "OPEN",
  CLOSED: "CLOSED",
};

let drawerState = DrawerState.CLOSED;

$(setupListeners);

$(window).on("resize", (e) => {
  if (e.target.innerWidth > remToPx(101.25)) {
    closeDrawer();
  }
});

function isNotLargeFormFactor() {
  return window.innerWidth < remToPx(101.25);
}

function openDrawer(doFade) {
  drawerState = DrawerState.OPEN;
  const transitionSpeed = doFade ? "fast" : 0;
  $(".overlay").fadeIn(transitionSpeed);
  $(".my-account__sidebar").fadeIn(transitionSpeed);
}

function closeDrawer(doFade) {
  drawerState = DrawerState.CLOSED;
  const transitionSpeed = doFade ? "fast" : 0;
  $(".overlay").fadeOut(transitionSpeed);
  $(".my-account__sidebar").fadeOut(transitionSpeed);
}

function setupListeners() {
  $(".my-account__title--sidebar").on("click", function (e) {
    closeDrawer(true);
  });

  $(".my-account__title--root").on("click", function (e) {
    if (isNotLargeFormFactor()) {
      openDrawer(true);
    }
  });

  $("body").on("click", (e) => {
    if (isNotLargeFormFactor() && drawerState == DrawerState.OPEN) {
      e.preventDefault();
      closeDrawer(true);
    }
  });

  $(".my-account__sidebar,.my-account__title--root").on("click", (e) => {
    e.stopPropagation();
  });
}

// Utility functions
/**
 * Function which gives the effective value in px for any value in rem unit.
 * Takes into account the current font size set in browser user preferences.
 * @param {number} rem The value in rem unit to be converted to pixels.
 */
const remToPx = (rem) => rem * parseFloat(getComputedStyle(document.documentElement).fontSize);
