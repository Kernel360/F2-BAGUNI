import { type NextRequest, NextResponse } from 'next/server';

export const config = {
  matcher: [
    '/((?!api|_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt|.*\\.png$|.*\\.svg$).*)',
  ],
};

export default async function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  const isAuthenticated = !!req.cookies.get('access_token');

  const unauthenticatedOnlyPaths = ['/login', '/share', '/landing'];
  const authenticatedOnlyPaths = ['/folders', '/recommend', '/mypage'];

  const isUnauthenticatedOnlyPath = unauthenticatedOnlyPaths.includes(pathname);
  const isAuthenticatedOnlyPath = authenticatedOnlyPaths.some((path) =>
    pathname.startsWith(path),
  );

  if (isUnauthenticatedOnlyPath && isAuthenticated) {
    return NextResponse.redirect(new URL('/', req.url));
  }

  if (
    (isAuthenticatedOnlyPath || pathname === '/') &&
    !isAuthenticated &&
    !isUnauthenticatedOnlyPath
  ) {
    return NextResponse.redirect(new URL('/landing', req.url));
  }

  if (isAuthenticated && pathname === '/') {
    return NextResponse.redirect(new URL('/recommend', req.url));
  }

  return NextResponse.next();
}
