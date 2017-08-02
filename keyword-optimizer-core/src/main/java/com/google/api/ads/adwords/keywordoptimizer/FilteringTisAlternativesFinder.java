package com.google.api.ads.adwords.keywordoptimizer;

/**
 * Created by jarednieder on 8/2/2017.
 */
public class FilteringTisAlternativesFinder extends TisAlternativesFinder {
    private final String filterString;

    /**
     * Creates a new {@link FilteringTisAlternativesFinder}.
     *
     * @param context holding shared objects during the optimization process
     */
    public FilteringTisAlternativesFinder(OptimizationContext context) {
        super(context);
        this.filterString = context.getConfiguration().getProperty("filterString").toString();
    }

    @Override
    public KeywordCollection derive(KeywordCollection keywords) throws KeywordOptimizerException {
        KeywordCollection results = super.derive(keywords);
        KeywordCollection filteredResults = new KeywordCollection(results.getCampaignConfiguration());
        for (KeywordInfo kwInfo : results) {
            if (kwInfo.getKeyword().getText().equals(filterString)) {
                filteredResults.add(kwInfo);
            }
        }
        return filteredResults;
    }
}
