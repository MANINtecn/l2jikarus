package net.sf.l2jdev.gameserver.model.interfaces;

public interface IParameterized<T>
{
	T getParameters();

	void setParameters(T var1);
}
