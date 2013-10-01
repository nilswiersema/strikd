package strikd.locale;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import strikd.locale.LocaleBundle.DictionaryType;

public class LocaleBundleManager
{
	private static final Logger logger = Logger.getLogger(LocaleBundleManager.class);
	
	private final File bundleDir;
	private Map<String, LocaleBundle> bundles = Collections.emptyMap();
	
	public LocaleBundleManager(File bundleDir)
	{
		if(!(bundleDir.isDirectory() && bundleDir.canRead()))
		{
			throw new IllegalArgumentException("bundleDir must be a readable directory");
		}
		
		this.bundleDir = bundleDir;
	}
	
	public void reload()
	{
		logger.info(String.format("reloading and indexing locales from %s...", this.bundleDir));
		
		// Load all bundles
		Map<String, LocaleBundle> bundles = new HashMap<String, LocaleBundle>();
		for(File file : this.bundleDir.listFiles())
		{
			if(file.isDirectory())
			{
				String locale = file.getName().trim();
				
				// Load bundle (all dictionaries in bundle)
				LocaleBundle bundle = new LocaleBundle(locale, file);
				if(bundle.getDictionary(DictionaryType.COMPLETE) == null)
				{
					logger.warn(String.format("not loaded %s, missing critical %s dict", locale, DictionaryType.COMPLETE));
				}
				else
				{
					bundles.put(locale, bundle);
					logger.info(String.format("loaded %s (%d dicts)", locale, bundle.size()));
				}
			}
		}
		
		// Found bundles?
		if(!bundles.isEmpty())
		{
			this.bundles = Collections.unmodifiableMap(bundles);
		}
		
		// Has bundles at all?
		if(this.bundles.isEmpty())
		{
			logger.fatal(String.format("no locale bundles at all!"));
			System.exit(0);
		}
	}
	
	public LocaleBundle getBundle(String locale)
	{
		return this.bundles.get(locale);
	}

	public Collection<LocaleBundle> getBundles()
	{
		return this.bundles.values();
	}
}
