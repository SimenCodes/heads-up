/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codes.simen.l50notifications.theme;

import android.view.ViewStub;
import android.widget.LinearLayout;

import codes.simen.l50notifications.R;

/**
 * Android L theme
 */
public class L5Black extends L5Dark {

    public L5Black(ViewStub stub) {
        super(stub);
    }

    @Override
    public void init(LinearLayout layout) {
        layout.findViewById(R.id.linearLayout).setBackgroundResource(R.drawable.card_black);
        super.init(layout);
    }


}
