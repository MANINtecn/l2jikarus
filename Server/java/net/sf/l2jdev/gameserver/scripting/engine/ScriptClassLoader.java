package net.sf.l2jdev.gameserver.scripting.engine;

import java.util.concurrent.ConcurrentHashMap;

public class ScriptClassLoader extends ClassLoader
{
	private final ConcurrentHashMap<String, ScriptClassData> _compiledClasses = new ConcurrentHashMap<>();

	public ScriptClassLoader(ClassLoader parent)
	{
		super(parent);
	}

	public void addCompiledClasses(Iterable<ScriptClassData> compiledClasses)
	{
		for (ScriptClassData compiledClass : compiledClasses)
		{
			this._compiledClasses.put(compiledClass.getJavaName(), compiledClass);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		ScriptClassData compiledClass = this._compiledClasses.get(name);
		if (compiledClass != null)
		{
			byte[] classBytes = compiledClass.getJavaData();
			return this.defineClass(name, classBytes, 0, classBytes.length);
		}
		return super.findClass(name);
	}
}
