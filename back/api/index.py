import os, sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "profinancas.settings")
os.environ.setdefault("SECRET_KEY", "dev-secret-change-me-in-prod")
os.environ.setdefault("DEBUG", "False")
os.environ.setdefault("ALLOWED_HOSTS", "*")
os.environ.setdefault("DJANGO_TESTING", "True")

from http.server import BaseHTTPRequestHandler

_django_app = None
def get_app():
    global _django_app
    if _django_app is None:
        import django
        django.setup()
        from django.core.wsgi import get_wsgi_application
        _django_app = get_wsgi_application()
    return _django_app

class handler(BaseHTTPRequestHandler):
    def do_GET(self): self._handle("GET")
    def do_POST(self): self._handle("POST")
    def do_PUT(self): self._handle("PUT")
    def do_PATCH(self): self._handle("PATCH")
    def do_DELETE(self): self._handle("DELETE")
    def do_OPTIONS(self): self._handle("OPTIONS")

    def _handle(self, method):
        from io import BytesIO
        try:
            app = get_app()
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length) if content_length > 0 else b""
            environ = {
                "REQUEST_METHOD": method,
                "SCRIPT_NAME": "",
                "PATH_INFO": self.path.split("?")[0],
                "QUERY_STRING": self.path.split("?", 1)[1] if "?" in self.path else "",
                "SERVER_NAME": "localhost",
                "SERVER_PORT": "80",
                "SERVER_PROTOCOL": "HTTP/1.1",
                "wsgi.version": (1, 0),
                "wsgi.url_scheme": "http",
                "wsgi.input": BytesIO(body),
                "wsgi.errors": sys.stderr,
                "wsgi.multithread": True,
                "wsgi.multiproc": False,
                "wsgi.run_once": False,
                "CONTENT_LENGTH": str(content_length),
                "CONTENT_TYPE": self.headers.get("Content-Type", ""),
            }
            for k, v in self.headers.items():
                environ["HTTP_" + k.upper().replace("-", "_")] = v

            response_status = [None]
            response_headers = [[]]
            def start_response(status, headers, exc_info=None):
                response_status[0] = status
                response_headers[0] = headers
                return lambda x: None

            result = app(environ, start_response)
            status_code = int(response_status[0].split(" ", 1)[0])
            self.send_response(status_code)
            for h in response_headers[0]:
                self.send_header(h[0], h[1])
            self.end_headers()
            for data in result:
                self.wfile.write(data)
            if hasattr(result, "close"):
                result.close()
        except Exception as e:
            import traceback
            self.send_response(500)
            self.send_header("Content-Type", "text/plain")
            self.end_headers()
            self.wfile.write(traceback.format_exc().encode())
