import javafx.util.Callback;

import java.util.*;
import java.util.concurrent.*;

abstract class TravelQuote {
    private void call() {
    }
}

abstract class TravelCompany {
    TravelQuote solicitQuote(TravelInfo travelInfo) {
        return null;
    }
}

class TravelInfo {
}

public class TravelBookingPortal {
    ExecutorService exec;

    private class QuoteTask implements Callback<TravelQuote> {
        TravelCompany company;
        TravelInfo travelInfo;

        QuoteTask(TravelCompany company, TravelInfo travelInfo) {
        }

        public TravelQuote call() throws Exception {
            return company.solicitQuote(travelInfo);
        }

    }

    public List<TravelQuote> getRankedTravelQuotes(
            TravelInfo travelInfo, Set<TravelCompany> companies,
            Comparator<TravelQuote> ranking, long time, TimeUnit unit)
            throws InterruptedException {
        List<QuoteTask> tasks = new ArrayList<QuoteTask>();
        for (TravelCompany company : companies)
            tasks.add(new QuoteTask(company, travelInfo));

        List<Future<TravelQuote>> futures =
                exec.invokeAll(tasks, time, unit);

        List<TravelQuote> quotes =
                new ArrayList<TravelQuote>(tasks.size());
        Iterator<QuoteTask> taskIterator = tasks.iterator();
        for (Future<TravelQuote> f : futures) {
            QuoteTask task = taskIterator.next();
            try {
                quotes.add(f.get());
            } catch (ExecutionException e) {
                quotes.add(task.getFailureQuote(e.getCause()));
            } catch (CancellationException e) {
                quotes.add(task.getTimeoutQuote(e));
            }
        }
        Collections.sort(quotes, ranking);
        return quotes;
    }
}
