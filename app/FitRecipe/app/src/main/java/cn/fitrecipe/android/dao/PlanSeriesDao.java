package cn.fitrecipe.android.dao;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import cn.fitrecipe.android.entity.SeriesPlan;

/**
 * Created by wk on 2015/9/4.
 */
public class PlanSeriesDao {

    private Dao<SeriesPlan, Integer> seriesPlanDaoOpe;
    private DatabaseHelper helper;

    public PlanSeriesDao(Context context) {
        try {
            helper = DatabaseHelper.getHelper(context);
            seriesPlanDaoOpe = helper.getDao(SeriesPlan.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(SeriesPlan seriesPlan) {
        try {
            seriesPlanDaoOpe.create(seriesPlan);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public SeriesPlan get(int id) {
        try {
            return seriesPlanDaoOpe.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
