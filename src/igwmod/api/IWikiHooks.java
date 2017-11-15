package igwmod.api;

public interface IWikiHooks{
    /**
     * Shows the Wiki gui at the given wiki location.
     * @param pageLocation location of the page, for example, 'minecraft:block/grass'
     */
    public void showWikiGui(String pageLocation);
}
