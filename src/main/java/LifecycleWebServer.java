import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

class LifecycleWebServer {
    private java.util.concurrent.ExecutorService exec;

    public LifecycleWebServer(ExecutorService exec) {
        this.exec = exec;
    }

    public void start() throws IOException {
        ServerSocket socket = new ServerSocket(80);
        while (!exec.isShutdown()) {
            try {
                final Socket conn = socket.accept();
                exec.execute(new Runnable() {
                    public void run() {
                        handleRequest(conn);
                    }
                });
            } catch (RejectedExecutionException e) {
                if (!exec.isShutdown()) {
                    System.out.println("task submission rejected");
                    System.out.println(e);
                }
            }
        }
    }

    public void stop() {
        exec.shutdown();
    }

    private void handleRequest(Socket connection) {
//        Request req = readRequest(connection);
//        if (isShutdownRequest(req))
//            stop();
//        else
//            dispatchRequest(req);
    }
}
